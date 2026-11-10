function fn() {
  var port = karate.properties['karate.server.port'] || '8080';
  var config = {
    baseUrl: 'http://localhost:' + port + '/api',
    port: port
  };
  karate.configure('connectTimeout', 10000);
  karate.configure('readTimeout', 10000);
  return config;
}
