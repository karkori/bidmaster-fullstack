Feature: Subida Segura de Imágenes, Validación Binaria (Magic Bytes) y Gestión de Objetos

  Background:
    * url baseUrl
    * def randomString = function(len) { var text = ''; var possible = 'abcdefghijklmnopqrstuvwxyz0123456789'; for (var i = 0; i < len; i++) text += possible.charAt(Math.floor(Math.random() * possible.length)); return text; }
    * def imageUser = 'img_user_' + randomString(6)
    * def userPayload =
      """
      {
        "username": "#(imageUser)",
        "email": "#(imageUser + '@test.com')",
        "password": "Password123!",
        "firstName": "Image",
        "lastName": "Tester",
        "role": "SELLER"
      }
      """

    # Registrar usuario propietario de las imágenes
    Given path 'users', 'register'
    And request userPayload
    When method post
    Then status 201
    * def userId = response.id

  Scenario: Subida exitosa de una imagen válida y recuperación de stream binario
    Given path 'images'
    And params { type: 'AUCTION', userId: '#(userId)' }
    And multipart file file = { read: 'classpath:test-data/valid.png', filename: 'valid.png', contentType: 'image/png' }
    When method post
    Then status 201
    And match response.id == '#notnull'
    And match response.originalFilename == 'valid.png'
    And match response.contentType == 'image/png'
    And match response.imageUrl == '#notnull'
    * def imageId = response.id

    # Consultar metadatos de la imagen por ID
    Given path 'images', imageId
    When method get
    Then status 200
    And match response.id == imageId
    And match response.uploadedBy == userId

    # Descargar contenido binario original
    Given path 'images', imageId, 'raw'
    When method get
    Then status 200
    And match header Content-Type contains 'image/png'

    # Eliminar imagen
    Given path 'images', imageId
    When method delete
    Then status 204

    # Verificar que tras el borrado devuelve 404 Not Found
    Given path 'images', imageId
    When method get
    Then status 404

  Scenario: Detección y bloqueo de archivo malicioso camuflado (spoofed PHP/text con extensión .png)
    # El archivo malicious.png contiene código PHP pero declara MIME image/png
    Given path 'images'
    And params { type: 'AUCTION', userId: '#(userId)' }
    And multipart file file = { read: 'classpath:test-data/malicious.png', filename: 'malicious.png', contentType: 'image/png' }
    When method post
    Then status 400
    And match response.title == 'Solicitud no válida'
    And match response.detail contains 'Tipo de archivo no válido'

  Scenario: Subida autenticada con JWT sin userId, isPrimary único, displayOrder y validación de propietario
    # Login del vendedor para obtener JWT
    Given path 'users', 'login'
    And request { username: '#(imageUser)', password: 'Password123!' }
    When method post
    Then status 200
    * def sellerAuth = { Authorization: '#("Bearer " + response.token)' }

    # Crear subasta en DRAFT
    * def endDate = java.time.LocalDateTime.now().plusDays(3).truncatedTo(java.time.temporal.ChronoUnit.SECONDS).toString()
    Given path 'auctions'
    And headers sellerAuth
    And request
      """
      {
        "title": "Subasta con Imagenes Reales",
        "description": "Producto de prueba para validar subida multiple e imagen principal",
        "category": "ELECTRONICS",
        "initialPrice": 250.00,
        "depositRequired": 25.00,
        "endDate": "#(endDate)",
        "condition": "like_new",
        "shippingOptions": "buyer_pays",
        "allowPause": true
      }
      """
    When method post
    Then status 201
    * def auctionId = response.id

    # Subir primera imagen con isPrimary=true y displayOrder=0 sin userId
    Given path 'images'
    And headers sellerAuth
    And multipart file file = { read: 'classpath:test-data/valid.png', filename: 'portada1.png', contentType: 'image/png' }
    And multipart field type = 'AUCTION'
    And multipart field referenceId = auctionId
    And multipart field isPrimary = 'true'
    And multipart field displayOrder = '0'
    When method post
    Then status 201
    And match response.uploadedBy == userId
    And match response.primary == true
    And match response.displayOrder == 0
    And match response.imageUrl contains '/raw'
    * def firstImageId = response.id

    # Subir segunda imagen con isPrimary=true y displayOrder=1 (debe desmarcar la anterior)
    Given path 'images'
    And headers sellerAuth
    And multipart file file = { read: 'classpath:test-data/valid.png', filename: 'portada2.png', contentType: 'image/png' }
    And multipart field type = 'AUCTION'
    And multipart field referenceId = auctionId
    And multipart field isPrimary = 'true'
    And multipart field displayOrder = '1'
    When method post
    Then status 201
    And match response.primary == true
    And match response.displayOrder == 1
    * def secondImageId = response.id

    # Verificar que la subasta devuelve ambas ordenadas y solo la segunda como primary
    Given path 'auctions', auctionId
    When method get
    Then status 200
    And match response.images == '#[2]'
    And match response.images[0].id == firstImageId
    And match response.images[0].primary == false
    And match response.images[1].id == secondImageId
    And match response.images[1].primary == true

    # Otro usuario autenticado no puede subir imagen a subasta ajena (403 Forbidden)
    * def otherUser = 'other_' + randomString(6)
    Given path 'users', 'register'
    And request
      """
      {
        "username": "#(otherUser)",
        "email": "#(otherUser + '@test.com')",
        "password": "Password123!",
        "firstName": "Other",
        "lastName": "Seller",
        "role": "SELLER"
      }
      """
    When method post
    Then status 201

    Given path 'users', 'login'
    And request { username: '#(otherUser)', password: 'Password123!' }
    When method post
    Then status 200
    * def otherAuth = { Authorization: '#("Bearer " + response.token)' }

    Given path 'images'
    And headers otherAuth
    And multipart file file = { read: 'classpath:test-data/valid.png', filename: 'intruso.png', contentType: 'image/png' }
    And multipart field type = 'AUCTION'
    And multipart field referenceId = auctionId
    When method post
    Then status 403

    # Otro usuario autenticado no puede cambiar portada, reordenar ni borrar imágenes ajenas (403 Forbidden)
    Given path 'images', firstImageId, 'primary'
    And headers otherAuth
    When method patch
    Then status 403

    Given path 'images', 'reference', auctionId, 'order'
    And param type = 'AUCTION'
    And headers otherAuth
    And request { imageIds: ['#(secondImageId)', '#(firstImageId)'] }
    When method patch
    Then status 403

    Given path 'images', secondImageId
    And headers otherAuth
    When method delete
    Then status 403

    # El vendedor propietario cambia la portada a la primera imagen por PATCH
    Given path 'images', firstImageId, 'primary'
    And headers sellerAuth
    When method patch
    Then status 200
    And match response.id == firstImageId
    And match response.primary == true

    # El vendedor propietario reordena las imágenes por PATCH manteniendo la nueva portada
    Given path 'images', 'reference', auctionId, 'order'
    And param type = 'AUCTION'
    And headers sellerAuth
    And request { imageIds: ['#(secondImageId)', '#(firstImageId)'] }
    When method patch
    Then status 200
    And match response == '#[2]'
    And match response[0].id == secondImageId
    And match response[0].displayOrder == 0
    And match response[0].primary == false
    And match response[1].id == firstImageId
    And match response[1].displayOrder == 1
    And match response[1].primary == true

    # Verificar el orden y portada persistidos al consultar la subasta
    Given path 'auctions', auctionId
    When method get
    Then status 200
    And match response.images[0].id == secondImageId
    And match response.images[0].primary == false
    And match response.images[1].id == firstImageId
    And match response.images[1].primary == true

    # Rechazo 400 cuando faltan imágenes o se incluye un ID ajeno en la reordenación
    Given path 'images', 'reference', auctionId, 'order'
    And param type = 'AUCTION'
    And headers sellerAuth
    And request { imageIds: ['#(secondImageId)'] }
    When method patch
    Then status 400
    And match response.title == 'Solicitud no válida'

    * def unknownImageId = java.util.UUID.randomUUID().toString()
    Given path 'images', 'reference', auctionId, 'order'
    And param type = 'AUCTION'
    And headers sellerAuth
    And request { imageIds: ['#(secondImageId)', '#(unknownImageId)'] }
    When method patch
    Then status 400

    # El propietario sí puede eliminar una imagen suya (204) y desaparece de la galería
    Given path 'images', secondImageId
    And headers sellerAuth
    When method delete
    Then status 204

    Given path 'images', secondImageId
    When method get
    Then status 404

  Scenario: Rechazo con 400 ProblemDetail cuando no se envía JWT ni userId
    Given path 'images'
    And multipart file file = { read: 'classpath:test-data/valid.png', filename: 'anon.png', contentType: 'image/png' }
    And multipart field type = 'AUCTION'
    When method post
    Then status 400
    And match response.title == 'Solicitud no válida'
    And match response.detail contains 'usuario autenticado'
