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

    # Pausa 1
    Given path 'auctions', auctionId, 'pause'
    And headers authHeader
    When method post
    Then status 200
    * match response.status == 'PAUSED'

    # Reanudar 1
    Given path 'auctions', auctionId, 'resume'
    And headers authHeader
    When method post
    Then status 200
    * match response.status == 'ACTIVE'

    # Pausa 2
    Given path 'auctions', auctionId, 'pause'
    And headers authHeader
    When method post
    Then status 200
    * match response.status == 'PAUSED'

    # Reanudar 2
    Given path 'auctions', auctionId, 'resume'
    And headers authHeader
    When method post
    Then status 200
    * match response.status == 'ACTIVE'

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
