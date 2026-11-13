Feature: Ciclo de Vida de Subastas, Modificación, Pausas y Búsquedas

  Background:
    * url baseUrl
    * def randomString = function(len) { var text = ''; var possible = 'abcdefghijklmnopqrstuvwxyz0123456789'; for (var i = 0; i < len; i++) text += possible.charAt(Math.floor(Math.random() * possible.length)); return text; }
    * def sellerUser = 'seller_' + randomString(6)
    * def sellerPayload =
      """
      {
        "username": "#(sellerUser)",
        "email": "#(sellerUser + '@test.com')",
        "password": "SellerPassword123!",
        "firstName": "Vendedor",
        "lastName": "Karate",
        "role": "SELLER"
      }
      """

    # Registrar e iniciar sesión como vendedor
    Given path 'users', 'register'
    And request sellerPayload
    When method post
    Then status 201
    * def sellerId = response.id

    Given path 'users', 'login'
    And request { username: '#(sellerUser)', password: 'SellerPassword123!' }
    When method post
    Then status 200
    * def sellerToken = response.token
    * def bearerToken = 'Bearer ' + sellerToken
    * def authHeader = { Authorization: '#(bearerToken)' }

  Scenario: Creación exitosa en DRAFT y validación de campos obligatorios
    * def endDate = java.time.LocalDateTime.now().plusDays(5).truncatedTo(java.time.temporal.ChronoUnit.SECONDS).toString()
    * def auctionPayload =
      """
      {
        "title": "Subasta Camara Canon EOS R5",
        "description": "Camara profesional en perfecto estado con lentes incluidos y garantia",
        "category": "COLLECTIBLES",
        "initialPrice": 1200.00,
        "minBidIncrement": 25.00,
        "depositRequired": 100.00,
        "endDate": "#(endDate)",
        "condition": "EXCELLENT",
        "shippingOptions": "SHIPPING_INCLUDED",
        "allowPause": true
      }
      """
    Given path 'auctions'
    And headers authHeader
    And request auctionPayload
    When method post
    Then status 201
    And match response.id == '#notnull'
    And match response.status == 'DRAFT'
    And match response.slug contains 'subasta-camara-canon-eos-r5'
    And match response.initialPrice == 1200.0
    And match response.currentPrice == 1200.0
    And match response.depositRequired == 100.0
    And match response.allowPause == true
    And match response.sellerId == sellerId

  Scenario: Rechazo de creación con datos inválidos
    * def invalidPayload =
      """
      {
        "title": "Abc",
        "description": "Corta",
        "category": "INVALID_CAT",
        "initialPrice": -50.00,
        "endDate": "2020-01-01T00:00:00"
      }
      """
    Given path 'auctions'
    And headers authHeader
    And request invalidPayload
    When method post
    Then status 400

  Scenario: Flujo completo de ciclo de vida: DRAFT -> PUBLICAR -> PAUSAR -> REANUDAR -> LÍMITE DE PAUSAS
    * def endDate = java.time.LocalDateTime.now().plusDays(3).truncatedTo(java.time.temporal.ChronoUnit.SECONDS).toString()
    * def payload =
      """
      {
        "title": "Subasta Mac Studio M2 Max",
        "description": "Estación de trabajo Apple M2 Max 64GB RAM 1TB SSD impecable",
        "category": "ELECTRONICS",
        "initialPrice": 1800.00,
        "minBidIncrement": 20.00,
        "depositRequired": 50.00,
        "endDate": "#(endDate)",
        "condition": "LIKE_NEW",
        "shippingOptions": "EXPRESS",
        "allowPause": true
      }
      """
    Given path 'auctions'
    And headers authHeader
    And request payload
    When method post
    Then status 201
    * def auctionId = response.id
    * match response.status == 'DRAFT'

    # Publicar subasta
    Given path 'auctions', auctionId, 'publish'
    And headers authHeader
    When method post
    Then status 200
    * match response.status == 'ACTIVE'
    * match response.startDate == '#notnull'
    * match response.pausesUsed == 0

    # Pausa 1
    Given path 'auctions', auctionId, 'pause'
    And headers authHeader
    When method post
    Then status 200
    * match response.status == 'PAUSED'
    * match response.pausesUsed == 1

    # Reanudar 1
    Given path 'auctions', auctionId, 'resume'
    And headers authHeader
    When method post
    Then status 200
    * match response.status == 'ACTIVE'
    * match response.pausesUsed == 1

    # Pausa 2
    Given path 'auctions', auctionId, 'pause'
    And headers authHeader
    When method post
    Then status 200
    * match response.status == 'PAUSED'
    * match response.pausesUsed == 2

    # Reanudar 2
    Given path 'auctions', auctionId, 'resume'
    And headers authHeader
    When method post
    Then status 200
    * match response.status == 'ACTIVE'
    * match response.pausesUsed == 2

    # Intento de Pausa 3 (Debe fallar porque el límite de pausas permitidas es 2)
    Given path 'auctions', auctionId, 'pause'
    And headers authHeader
    When method post
    Then status 400

  Scenario: Búsqueda y filtrado de subastas
    * def endDate = java.time.LocalDateTime.now().plusDays(4).truncatedTo(java.time.temporal.ChronoUnit.SECONDS).toString()
    * def searchTitle = 'Reloj Vintage ' + randomString(4)
    * def itemPayload =
      """
      {
        "title": "#(searchTitle)",
        "description": "Reloj suizo original con certificado de garantia y estuche incluido",
        "category": "COLLECTIBLES",
        "initialPrice": 3500.00,
        "endDate": "#(endDate)",
        "condition": "EXCELLENT",
        "shippingOptions": "STANDARD",
        "allowPause": false
      }
      """
    Given path 'auctions'
    And headers authHeader
    And request itemPayload
    When method post
    Then status 201
    * def watchId = response.id

    # Publicar para que sea visible
    Given path 'auctions', watchId, 'publish'
    And headers authHeader
    When method post
    Then status 200

    # Consultar por ID
    Given path 'auctions', watchId
    When method get
    Then status 200
    And match response.id == watchId
    And match response.title == searchTitle
    And match response.category == 'Coleccionables'

    # Filtrar por categoría
    Given path 'auctions', 'category', 'COLLECTIBLES'
    When method get
    Then status 200
    And match response[*].id contains watchId

  Scenario: Edición PUT de borrador con displayName, rechazo 400, cancelación y eliminación de borrador
    * def endDate = java.time.LocalDateTime.now().plusDays(6).truncatedTo(java.time.temporal.ChronoUnit.SECONDS).toString()
    * def updatedEndDate = java.time.LocalDateTime.now().plusDays(8).truncatedTo(java.time.temporal.ChronoUnit.SECONDS).toString()

    # Crear borrador inicial
    Given path 'auctions'
    And headers authHeader
    And request
      """
      {
        "title": "Borrador Inicial Subasta",
        "description": "Descripción inicial del borrador que será editado posteriormente",
        "category": "COLLECTIBLES",
        "initialPrice": 150.00,
        "endDate": "#(endDate)",
        "condition": "GOOD",
        "shippingOptions": "STANDARD",
        "allowPause": false
      }
      """
    When method post
    Then status 201
    * def draftId = response.id

    # Actualizar borrador enviando displayName de categoría ('Electrónica')
    Given path 'auctions', draftId
    And headers authHeader
    And request
      """
      {
        "title": "Borrador Editado Electrónica",
        "description": "Descripción actualizada del producto electrónico en perfecto estado",
        "category": "Electrónica",
        "initialPrice": 220.00,
        "minBidIncrement": 10.00,
        "depositRequired": 15.00,
        "endDate": "#(updatedEndDate)",
        "condition": "LIKE_NEW",
        "shippingOptions": "EXPRESS",
        "allowPause": true
      }
      """
    When method put
    Then status 200
    And match response.title == 'Borrador Editado Electrónica'
    And match response.category == 'Electrónica'
    And match response.initialPrice == 220.0
    And match response.allowPause == true
    And match response.pausesUsed == 0

    # Rechazo 400 al actualizar con categoría desconocida
    Given path 'auctions', draftId
    And headers authHeader
    And request
      """
      {
        "title": "Borrador Categoría Inválida",
        "description": "Descripción válida para probar el rechazo de categoría inválida",
        "category": "CATEGORIA_DESCONOCIDA",
        "initialPrice": 220.00,
        "endDate": "#(updatedEndDate)",
        "condition": "GOOD",
        "shippingOptions": "STANDARD"
      }
      """
    When method put
    Then status 400
    And match response.title == 'Solicitud no válida'

    # Crear segundo borrador y eliminarlo
    Given path 'auctions'
    And headers authHeader
    And request
      """
      {
        "title": "Borrador Para Eliminar",
        "description": "Este borrador será eliminado directamente sin publicarse",
        "category": "OTHERS",
        "initialPrice": 40.00,
        "endDate": "#(endDate)",
        "condition": "GOOD",
        "shippingOptions": "STANDARD"
      }
      """
    When method post
    Then status 201
    * def deleteDraftId = response.id

    Given path 'auctions', deleteDraftId
    And headers authHeader
    When method delete
    Then status 204

    Given path 'auctions', deleteDraftId
    When method get
    Then status 404

    # Publicar primer borrador y comprobar que no se puede editar ni eliminar en ACTIVE, pero sí cancelar
    Given path 'auctions', draftId, 'publish'
    And headers authHeader
    When method post
    Then status 200
    And match response.status == 'ACTIVE'

    Given path 'auctions', draftId
    And headers authHeader
    And request
      """
      {
        "title": "Intento Edicion Activa",
        "description": "No debería permitirse editar una subasta que ya está activa",
        "category": "ELECTRONICS",
        "initialPrice": 250.00,
        "endDate": "#(updatedEndDate)",
        "condition": "GOOD",
        "shippingOptions": "STANDARD"
      }
      """
    When method put
    Then status 400
    And match response.title == 'Operación no permitida en el estado actual'

    Given path 'auctions', draftId, 'cancel'
    And headers authHeader
    When method post
    Then status 200
    And match response.status == 'CANCELLED'
