Feature: Gestión de Usuarios, Autenticación JWT y Operaciones de Saldo

  Background:
    * url baseUrl
    * def randomString = function(len) { var text = ''; var possible = 'abcdefghijklmnopqrstuvwxyz0123456789'; for (var i = 0; i < len; i++) text += possible.charAt(Math.floor(Math.random() * possible.length)); return text; }

  Scenario: Registro exitoso de un nuevo usuario
    * def uniqueUser = 'buyer_' + randomString(6)
    * def userPayload =
      """
      {
        "username": "#(uniqueUser)",
        "email": "#(uniqueUser + '@karate-test.com')",
        "password": "Password123!",
        "firstName": "Karate",
        "lastName": "User",
        "role": "BUYER"
      }
      """
    Given path 'users', 'register'
    And request userPayload
    When method post
    Then status 201
    And match response.id == '#notnull'
    And match response.username == uniqueUser
    And match response.email == uniqueUser + '@karate-test.com'
    And match response.status == 'ACTIVE'
    And match response.role == 'BUYER'
    And match response.balance == 0.0

  Scenario: Validación de campos obligatorios y formato en registro
    * def invalidPayload =
      """
      {
        "username": "ab",
        "email": "not-an-email",
        "password": "123",
        "firstName": "",
        "lastName": "",
        "role": "BUYER"
      }
      """
    Given path 'users', 'register'
    And request invalidPayload
    When method post
    Then status 400
    And match response.title == 'Datos de entrada no válidos'
    And match response.errors != null

  Scenario: Rechazo de duplicados de nombre de usuario
    * def uniqueUser = 'dup_' + randomString(6)
    * def payload =
      """
      {
        "username": "#(uniqueUser)",
        "email": "#(uniqueUser + '@karate-test.com')",
        "password": "Password123!",
        "firstName": "Duplicate",
        "lastName": "Test",
        "role": "BUYER"
      }
      """
    # Primer registro exitoso
    Given path 'users', 'register'
    And request payload
    When method post
    Then status 201

    # Segundo registro con mismo username y correo distinto -> rechazo 400
    * def dupPayload =
      """
      {
        "username": "#(uniqueUser)",
        "email": "#('other_' + uniqueUser + '@karate-test.com')",
        "password": "Password123!",
        "firstName": "Duplicate",
        "lastName": "Test",
        "role": "BUYER"
      }
      """
    Given path 'users', 'register'
    And request dupPayload
    When method post
    Then status 400

  Scenario: Autenticación exitosa genera token JWT válido
    * def uniqueUser = 'auth_' + randomString(6)
    * def regPayload =
      """
      {
        "username": "#(uniqueUser)",
        "email": "#(uniqueUser + '@karate-test.com')",
        "password": "MySecretPassword123!",
        "firstName": "Auth",
        "lastName": "Tester",
        "role": "BUYER"
      }
      """
    Given path 'users', 'register'
    And request regPayload
    When method post
    Then status 201

    # Login exitoso
    Given path 'users', 'login'
    And request { username: '#(uniqueUser)', password: 'MySecretPassword123!' }
    When method post
    Then status 200
    And match response.token == '#notnull'
    And match response.tokenType == 'Bearer'
    And match response.username == uniqueUser
    And match response.role == 'BUYER'

  Scenario: Credenciales incorrectas y bloqueo por fuerza bruta tras 5 intentos
    * def uniqueUser = 'lock_' + randomString(6)
    * def regPayload =
      """
      {
        "username": "#(uniqueUser)",
        "email": "#(uniqueUser + '@karate-test.com')",
        "password": "CorrectPassword123!",
        "firstName": "Lock",
        "lastName": "Tester",
        "role": "BUYER"
      }
      """
    Given path 'users', 'register'
    And request regPayload
    When method post
    Then status 201
    * def userId = response.id

    # 5 intentos fallidos consecutivos con contraseña errónea para este usuario específico
    Given path 'users', 'login'
    And request { username: '#(uniqueUser)', password: 'WrongPassword' }
    When method post
    Then status 401

    Given path 'users', 'login'
    And request { username: '#(uniqueUser)', password: 'WrongPassword' }
    When method post
    Then status 401

    Given path 'users', 'login'
    And request { username: '#(uniqueUser)', password: 'WrongPassword' }
    When method post
    Then status 401

    Given path 'users', 'login'
    And request { username: '#(uniqueUser)', password: 'WrongPassword' }
    When method post
    Then status 401

    Given path 'users', 'login'
    And request { username: '#(uniqueUser)', password: 'WrongPassword' }
    When method post
    Then status 401

    # Verificar que el usuario ahora está BLOCKED en base de datos
    Given path 'users', userId
    When method get
    Then status 200
    And match response.status == 'BLOCKED'

    # Reactivar usuario
    Given path 'users', userId, 'status', 'ACTIVE'
    When method patch
    Then status 204

    # Verificar que vuelve a estar ACTIVE
    Given path 'users', userId
    When method get
    Then status 200
    And match response.status == 'ACTIVE'

  Scenario: Operaciones de saldo (añadir, restar y validación de fondos insuficientes)
    * def uniqueUser = 'wallet_' + randomString(6)
    * def regPayload =
      """
      {
        "username": "#(uniqueUser)",
        "email": "#(uniqueUser + '@karate-test.com')",
        "password": "WalletPassword123!",
        "firstName": "Wallet",
        "lastName": "User",
        "role": "BUYER"
      }
      """
    Given path 'users', 'register'
    And request regPayload
    When method post
    Then status 201
    * def userId = response.id

    # Añadir 250.00
    Given path 'users', userId, 'balance', 'add'
    And request { amount: 250.00 }
    When method post
    Then status 200

    Given path 'users', userId
    When method get
    Then status 200
    And match response.balance == 250.0

    # Restar 50.00
    Given path 'users', userId, 'balance', 'subtract'
    And request { amount: 50.00 }
    When method post
    Then status 200

    Given path 'users', userId
    When method get
    Then status 200
    And match response.balance == 200.0

    # Restar más de lo disponible (300.00) -> 400 Bad Request
    Given path 'users', userId, 'balance', 'subtract'
    And request { amount: 300.00 }
    When method post
    Then status 400
