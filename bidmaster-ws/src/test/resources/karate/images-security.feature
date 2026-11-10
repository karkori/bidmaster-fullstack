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
