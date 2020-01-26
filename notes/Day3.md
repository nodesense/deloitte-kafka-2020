# Kafka Connectors

Source Connector  -read from existing sources like file, db and publish to kafka topics
Sink Connector - read from kafka topics and write to a file, db, elastic search

Source --> Source Connector --> Brokers --> Sink Connectors --> Sink

File connector / Hello World

InputFile [source]
Kafka to detect changes [new lines] and publish to kafka

Output [sink]
Kafka to consume the data and write to file

Linux Machine/Putty/SSH into Linux machine

Commands

confluent start

confluent status

confluent list connectors

confluent status connectors

# Using File Source Connectors

# NANO EDITOR

    Ctrl + X - To Quit, it will to save or leave without saving file
    Ctrl + W  - Write to file

Create a property file for connector configureation/Source connector

> touch file-source.properties
> nano file-source.properties

and below content 

name=stock-file-source
connector.class=FileStreamSource
tasks.max=1
file=/root/stocks.csv
topic=stocks


#DONE


touch stocks.csv

How to load the connectors into kafka?, this will load source connector, keep watching for 
stocks.csv changes

confluent load stock-file-source -d file-source.properties

Check whether connector is running or not

confluent status connectors

To know specific connector status

confluent status stock-file-source

Put some data into csv file

echo "1234,10" >> stocks.csv

echo "1235,20" >> stocks.csv

echo "1236,30" >> stocks.csv


cat stocks.csv



# Second command prompt in your Windows/Linux Putty shell

kafka-console-consumer --bootstrap-server k5.nodesense.ai:9092 --topic stocks --from-beginning


# FILE SINK Connectors

Consume data from Kafka Topics and write to file


touch output-file.csv


touch file-sink.properties

nano file-sink.properties

paste below content


name=stock-file-sink
connector.class=FileStreamSink
tasks.max=1
file=/root/output-file.csv
topics=stocks


### DONE

confluent load stock-file-sink -d file-sink.properties

confluent status stock-file-sink

cat output-file.csv


echo "1237,30" >> stocks.csv

confluent status connectors

####

touch greetings.txt



touch greetings-file-sink.properties

nano greetings-file-sink.properties

paste below content


name=greetings-file-sink
connector.class=FileStreamSink
tasks.max=1
file=/root/greetings.text
topics=greetings
key.converter=org.apache.kafka.connect.storage.StringConverter
value.converter=org.apache.kafka.connect.storage.StringConverter

## Done


confluent load greetings-file-sink -d greetings-file-sink.properties
confluent status greetings-file-sink
confluent unload greetings-file-sink

Then run the SimpleProducer.java, produce to topic greetings

## One last example Avro and file sink

touch invoices.txt


touch invoices-file-sink.properties

nano invoices-file-sink.properties

paste below content


name=invoices-file-sink
connector.class=FileStreamSink
tasks.max=1
file=/root/invoices.txt
topics=invoices
key.converter=io.confluent.connect.avro.AvroConverter
value.converter=io.confluent.connect.avro.AvroConverter
key.converter.schema.registry.url=http://k5.nodesense.ai:8081
value.converter.schema.registry.url=http://k5.nodesense.ai:8081

## DONE


confluent load invoices-file-sink -d invoices-file-sink.properties
confluent status invoices-file-sink
 
cat invoices.txt

================

MYSQL

MySQL JDBC not included in Kafka Distribution

Devleopers to download jdbc jars of oracle/mysql/ms sql


wget https://dev.mysql.com/get/Downloads/Connector-J/mysql-connector-java-5.1.47.tar.gz

tar xf mysql-connector-java-5.1.47.tar.gz
cp mysql-connector-java-5.1.47/*.jar confluent-5.2.2/share/java/kafka-connect-jdbc


                       
Implmenting MYSQL Source connector
 Whenever new record inserted/updated, publish to kafka
 JDBC Datasources, can use Avro, Schema registry
 It automatically build avro schema from database table
 
 
## Setup the database 

mysql -uroot


CREATE USER 'team'@'localhost' IDENTIFIED BY 'team1234';

CREATE DATABASE ecommerce; 

GRANT ALL PRIVILEGES ON ecommerce.* TO 'team'@'localhost';


Exit from the shell

mysql -uroot

USE ecommerce;

-- detect insert/update changes using timestamp , but HARD delete

create table products (id int, 
                       name varchar(255), 
                       price int, 
                       create_ts timestamp DEFAULT CURRENT_TIMESTAMP, 
                       update_ts timestamp DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP );
                       
             
             
Settings can be done in json or properties file

### START PROPERTY JSON CONFIGURATION
 
touch mysql-product-source.json
 
nano  mysql-product-source.json
   
   paste below
   
   {
   "name": "mysql-product-source",
   "config": {
     "connector.class": "io.confluent.connect.jdbc.JdbcSourceConnector",
     "key.converter": "io.confluent.connect.avro.AvroConverter",
     "key.converter.schema.registry.url": "http://k5.nodesense.ai:8081",
     "value.converter": "io.confluent.connect.avro.AvroConverter",
     "value.converter.schema.registry.url": "http://k5.nodesense.ai:8081",
     "connection.url": "jdbc:mysql://localhost:3306/ecommerce?user=team&password=team1234",
     "_comment": "Which table(s) to include",
     "table.whitelist": "products",
     "mode": "timestamp",
      "timestamp.column.name": "update_ts",
     "validate.non.null": "false",
     "_comment": "The Kafka topic will be made up of this prefix, plus the table name  ",
     "topic.prefix": "db_"
   }
 }
 
 
### END PROPERTY JSON CONFIGURATION


 Note: the topic shall be <<PREFIX>>+<<TableName>> example: db_products
 
 
 confluent load mysql-product-source -d mysql-product-source.json
 
 confluent status connectors
 
 confluent status mysql-product-source
 

### Run below on your system or different putty/ssh

 kafka-avro-console-consumer --bootstrap-server k5.nodesense.ai:9092 --topic db_products --from-beginning --property schema.registry.url="http://k5.nodesense.ai:8081"


### Use Different Putty 

 
 mysql -uroot
 
 USE ecommerce;


 insert into products (id, name, price) values(1,'google phone', 2000); 
 
 
 insert into products (id, name, price) values(2,'nexus phone', 3000); 
 
 update products set price=3333 where id=1; 


### CHECK IN Browser for Schema registration


 go and check http://k5.nodesense.ai:8081/subjects  
 to ensure that you have schema for products listed
 
 http://k5.nodesense.ai:8081/subjects/db_products-value/versions/1
 
  
  java -jar ./lib/avro-tools-1.8.2.jar compile schema ./src/main/resources/avro/product.avsc ./src/main/java

## MYSQL SINK Connectors
  Consume from Topics, write to database
  kafka-avro-console-producers
  
  
  
touch  mysql-product-sink.properties

nano  mysql-product-sink.properties

name=mysql-product-sink
connector.class=io.confluent.connect.jdbc.JdbcSinkConnector
tasks.max=1
topics=products
connection.url=jdbc:mysql://localhost:3306/ecommerce?user=team&password=team1234
auto.create=true
key.converter=io.confluent.connect.avro.AvroConverter
key.converter.schema.registry.url=http://k5.nodesense.ai:8081
value.converter=io.confluent.connect.avro.AvroConverter
value.converter.schema.registry.url=http://k5.nodesense.ai:8081


consume from products topics, write to products table

To write  to specific table name,

use belwo property

table.name.format=mst_${topic}

table.name.format=somename_table


confluent load mysql-product-sink -d  mysql-product-sink.properties

Check with below command.

kafka-avro-console-producer --broker-list k5.nodesense.ai:9092 --topic products --property value.schema='{"type":"record","name":"product","fields":[{"name":"id","type":"int"},{"name":"name", "type": "string"}, {"name":"price", "type": "int"}]}'  --property schema.registry.url="http://k5.nodesense.ai:8081"
   

paste below line one after another without new line

{"id": 999, "name": "asus pro", "price": 100}


-- in db,

select * from products;

check whether id 999 appears ot not


# REST PROXY

 PROXY between Http Client and Kafka Broker
 Runs on Port : 8082
 
 exposes end points for accessing kafka
 
 Allowing constrained/non-native kafka applications to consume/produce data with kafka
 
 
 List all topics
 
 Try with Putty/Shell
 
 
 http://k5.nodesense.ai:8082/topics
 
 
 Describe a topic
 
 http://k5.nodesense.ai:8082/topics/greetings
 
 
kafka-console-consumer --bootstrap-server k5.nodesense.ai:9092 --topic greetings 

    prints value protion
        {"name":"testUser3"}
        

 curl -X POST -H "Content-Type: application/vnd.kafka.json.v2+json" \
           --data '{"records":[{"value":{"name": "testUser"}}]}' \
           "http://k5.nodesense.ai:8082/topics/greetings"
           
           
 
 curl -X POST -H "Content-Type: application/vnd.kafka.json.v2+json" \
           --data '{"records":[{"value":{"name": "testUser2"}}]}' \
           "http://k5.nodesense.ai:8082/topics/greetings"
           
           
 
 curl -X POST -H "Content-Type: application/vnd.kafka.json.v2+json" \
           --data '{"records":[{"value":{"name": "testUser3"}}]}' \
           "http://k5.nodesense.ai:8082/topics/greetings"
           
           
 With KEY Example
 
 kafka-console-consumer --bootstrap-server k5.nodesense.ai:9092 --topic greetings    --property print.key=true

 
 curl -X POST -H "Content-Type: application/vnd.kafka.json.v2+json" \
           --data '{"records":[{"key":"User10","value":{"name": "user10"}}]}' \
           "http://k5.nodesense.ai:8082/topics/greetings"
           
           
 Susbcribe using REST PROXY
 as HTTP is stateless, not always connected protocol, pull based
 
 CREATE CONSUMER INSTANCE
 
curl -X POST -H "Content-Type: application/vnd.kafka.v2+json" -H "Accept: application/vnd.kafka.v2+json" \
    --data '{"name": "greetings_kafka_http_client2", "format": "binary", "auto.offset.reset": "earliest"}' \
    http://k5.nodesense.ai:8082/consumers/greetings2_group
    
    got response
    
    {"instance_id":"greetings_kafka_http_client1","base_uri":"http://k5.nodesense.ai:8082/consumers/greetings_group/instances/greetings_kafka_http_client1"}%
    
 Subscribe the consumer to a topic , need topic name
 
 curl -X POST -H "Content-Type: application/vnd.kafka.v2+json" --data '{"topics":["greetings"]}' \
     http://k5.nodesense.ai:8082/consumers/greetings2_group/instances/greetings_kafka_http_client2/subscription
    
    
 To consume the data
 
 curl -X GET -H "Accept: application/vnd.kafka.json.v2+json" \
     http://k5.nodesense.ai:8082/consumers/greetings2_group/instances/greetings_kafka_http_client2/records
     
 Delete subscription
 
 curl -X DELETE -H "Accept: application/vnd.kafka.v2+json" \
           http://localhost:8082/consumers/greetings_group/instances/greetings_kafka_http_client1
           
           