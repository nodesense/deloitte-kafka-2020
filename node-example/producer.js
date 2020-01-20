var kafka = require('kafka-node');

var   Producer = kafka.Producer;
var   KeyedMessage = kafka.KeyedMessage;

var    client = new kafka.KafkaClient({kafkaHost: 'localhost:9092'});

var     producer = new Producer(client);

 // IN is key
 // 'India Greeting fron Node.js' is value
 var km = new KeyedMessage('IN', 'India Greeting fron Node.js');

 var    payloads = [
        { topic: 'greetings', messages: 'hi', partition: 2 },
        { topic: 'greetings', messages: ['hello', 'world', km], partition: 1 }
 ];

producer.on('ready', function () {
    producer.send(payloads, function (err, data) {
        console.log(data);
    });
});

producer.on('error', function (err) {})