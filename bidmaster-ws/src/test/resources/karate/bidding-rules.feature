Feature: Reglas de Puja, Depósitos de Garantía y Liberación Automática al Ser Sobrepujado

  Background:
    * url baseUrl
    * def randomString = function(len) { var text = ''; var possible = 'abcdefghijklmnopqrstuvwxyz0123456789'; for (var i = 0; i < len; i++) text += possible.charAt(Math.floor(Math.random() * possible.length)); return text; }

    # Registrar Vendedor
    * def sellerUser = 'seller_bid_' + randomString(6)
    * def sellerPayload =
      """
      {
        "username": "#(sellerUser)",
        "email": "#(sellerUser + '@test.com')",
        "password": "Password123!",
        "firstName": "Seller",
        "lastName": "Bids",
        "role": "SELLER"
      }
      """
    Given path 'users', 'register'
    And request sellerPayload
    When method post
    Then status 201
    * def sellerId = response.id

    Given path 'users', 'login'
    And request { username: '#(sellerUser)', password: 'Password123!' }
    When method post
    Then status 200
    * def sellerToken = response.token
    * def sellerBearer = 'Bearer ' + sellerToken
    * def sellerAuth = { Authorization: '#(sellerBearer)' }

    # Registrar Comprador 1 (con 500.00 de saldo)
    * def buyer1User = 'buyer1_' + randomString(6)
    * def buyer1Payload =
      """
      {
        "username": "#(buyer1User)",
        "email": "#(buyer1User + '@test.com')",
        "password": "Password123!",
        "firstName": "Buyer1",
        "lastName": "First",
        "role": "BUYER"
      }
      """
    Given path 'users', 'register'
    And request buyer1Payload
    When method post
    Then status 201
    * def buyer1Id = response.id

    Given path 'users', buyer1Id, 'balance', 'add'
    And request { amount: 500.00 }
    When method post
    Then status 200

    Given path 'users', 'login'
    And request { username: '#(buyer1User)', password: 'Password123!' }
    When method post
    Then status 200
    * def buyer1Token = response.token
    * def buyer1Bearer = 'Bearer ' + buyer1Token
    * def buyer1Auth = { Authorization: '#(buyer1Bearer)' }

    # Registrar Comprador 2 (inicialmente con 10.00 de saldo)
    * def buyer2User = 'buyer2_' + randomString(6)
    * def buyer2Payload =
      """
      {
        "username": "#(buyer2User)",
        "email": "#(buyer2User + '@test.com')",
        "password": "Password123!",
        "firstName": "Buyer2",
        "lastName": "Second",
        "role": "BUYER"
      }
      """
    Given path 'users', 'register'
    And request buyer2Payload
    When method post
    Then status 201
    * def buyer2Id = response.id

    Given path 'users', buyer2Id, 'balance', 'add'
    And request { amount: 10.00 }
    When method post
    Then status 200

    Given path 'users', 'login'
    And request { username: '#(buyer2User)', password: 'Password123!' }
    When method post
    Then status 200
    * def buyer2Token = response.token
    * def buyer2Bearer = 'Bearer ' + buyer2Token
    * def buyer2Auth = { Authorization: '#(buyer2Bearer)' }

  Scenario: Flujo transaccional completo de pujas con depósito y sobrepuja
    # 1. Crear subasta con precio 100, incremento 10, depósito 50
    * def endDate = java.time.LocalDateTime.now().plusDays(2).truncatedTo(java.time.temporal.ChronoUnit.SECONDS).toString()
    * def payload =
      """
      {
        "title": "Subasta Tarjeta Grafica RTX 4080",
        "description": "GPU en caja original precintada con garantia oficial de distribuidor",
        "category": "ELECTRONICS",
        "initialPrice": 100.00,
        "minBidIncrement": 10.00,
        "depositRequired": 50.00,
        "endDate": "#(endDate)",
        "condition": "NEW",
        "shippingOptions": "EXPRESS"
      }
      """
    Given path 'auctions'
    And headers sellerAuth
    And request payload
    When method post
    Then status 201
    * def auctionId = response.id

    # Intentar pujar en estado DRAFT -> debe fallar 400
    Given path 'auctions', auctionId, 'bids'
    And headers buyer1Auth
    And request { amount: 110.00 }
    When method post
    Then status 400

    # Publicar subasta
    Given path 'auctions', auctionId, 'publish'
    And headers sellerAuth
    When method post
    Then status 200

    # El vendedor intenta autopujar en su propia subasta -> debe fallar 403 Forbidden
    Given path 'auctions', auctionId, 'bids'
    And headers sellerAuth
    And request { amount: 110.00 }
    When method post
    Then status 403

    # 2. Buyer1 realiza puja válida de 110.00
    Given path 'auctions', auctionId, 'bids'
    And headers buyer1Auth
    And request { amount: 110.00 }
    When method post
    Then status 200
    And match response.currentPrice == 110.0
    And match response.winnerId == buyer1Id
    And match response.totalBids == 1

    # Verificar que a Buyer1 se le bloquearon 50.00 de depósito (saldo disponible 450, bloqueado 50)
    Given path 'users', buyer1Id
    When method get
    Then status 200
    And match response.balance == 450.0
    And match response.blockedBalance == 50.0

    # 3. Buyer2 intenta pujar 130.00 pero solo tiene 10.00 de saldo (necesita 50 de depósito) -> falla 400
    Given path 'auctions', auctionId, 'bids'
    And headers buyer2Auth
    And request { amount: 130.00 }
    When method post
    Then status 400

    # Buyer2 recarga fondos (añade 200.00 -> total 210.00)
    Given path 'users', buyer2Id, 'balance', 'add'
    And request { amount: 200.00 }
    When method post
    Then status 200

    # Buyer2 ahora puja 130.00 con fondos suficientes -> éxito
    Given path 'auctions', auctionId, 'bids'
    And headers buyer2Auth
    And request { amount: 130.00 }
    When method post
    Then status 200
    And match response.currentPrice == 130.0
    And match response.winnerId == buyer2Id
    And match response.totalBids == 2

    # Verificar que Buyer2 tiene bloqueados sus 50.00 de depósito
    Given path 'users', buyer2Id
    When method get
    Then status 200
    And match response.balance == 160.0
    And match response.blockedBalance == 50.0

    # CRUCIAL: Verificar que a Buyer1 (sobrepujado) se le devolvieron/desbloquearon sus 50.00 automáticamente
    Given path 'users', buyer1Id
    When method get
    Then status 200
    And match response.balance == 500.0
    And match response.blockedBalance == 0.0

    # 4. Buyer1 intenta pujar por debajo del mínimo (precio actual 130 + minIncrement 10 = 140 requerido)
    Given path 'auctions', auctionId, 'bids'
    And headers buyer1Auth
    And request { amount: 135.00 }
    When method post
    Then status 400

    # 5. Finalizar subasta
    Given path 'auctions', auctionId, 'finish'
    And headers sellerAuth
    When method post
    Then status 200
    And match response.status == 'FINISHED'
    And match response.winnerId == buyer2Id

    # Intentar pujar en subasta finalizada -> falla 400
    Given path 'auctions', auctionId, 'bids'
    And headers buyer1Auth
    And request { amount: 150.00 }
    When method post
    Then status 400

    # 6. Consultar historial de pujas de la subasta
    Given path 'auctions', auctionId, 'bids'
    When method get
    Then status 200
    And match response == '#[2]'
    And match response[0].amount == 130.0
    And match response[0].status == 'WON'
    And match response[1].amount == 110.0
    And match response[1].status == 'OUTBID'
