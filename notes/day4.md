# STREAMS


// Take pom.xml dependencies
// WordCountStream.java

kafka-topics --zookeeper k5.nodesense.ai:2181 --create --topic words --replication-factor 1 --partitions 3

kafka-console-producer --broker-list k5.nodesense.ai:9092 --topic words
    

Run the Java application
 
See the output but in binary


kafka-console-consumer --bootstrap-server k5.nodesense.ai:9092 --topic words-count-output --from-beginning --property print.key=true  --property value.deserializer=org.apache.kafka.common.serialization.LongDeserializer


kafka-console-consumer --bootstrap-server k5.nodesense.ai:9092 --topic words-count-windowed-output --from-beginning --property print.key=true  --property value.deserializer=org.apache.kafka.common.serialization.LongDeserializer



Run the WordStrema Java application


---
Invoices Stream

Run the Invoice Producer

Run the InvoiceStream application

Run the consume consumer

kafka-console-consumer --bootstrap-server k5.nodesense.ai:9092 --topic statewise-invoices-count --from-beginning --property print.key=true  --property value.deserializer=org.apache.kafka.common.serialization.LongDeserializer

   kafka-console-consumer --bootstrap-server k5.nodesense.ai:9092 --topic statewise-amount --from-beginning --property print.key=true  --property value.deserializer=org.apache.kafka.common.serialization.LongDeserializer

 

# KSQL 

## Preparation

Launch Putty Shell 1

below produce the records every 5 seconds, write to topic users

ksql-datagen quickstart=users format=avro topic=users maxInterval=5000


http://k5.nodesense.ai:8081/subjects/users-value/versions/1

Launch Putty Shell 2

below produce the records every 5 seconds, write to topic pageviews


ksql-datagen quickstart=pageviews format=avro topic=pageviews maxInterval=5000

http://k5.nodesense.ai:8081/subjects/pageviews-value/versions/1

# KSQL Shell /Putty/SSH

ksql 

prompt will appear

SHOW STREAMS;

SHOW TABLES;

CREATE STREAM users_stream (userid varchar, regionid varchar, gender varchar) WITH (kafka_topic='users', value_format='AVRO');

SHOW STREAMS;

DESCRIBE users_stream;

NON_PERSISTED QUERIES [Means, the output/result is not stored into KAfka Brokers]

select userid, regionid, gender from users_stream;

select userid, regionid, gender from users_stream where gender='FEMALE';

select userid, regionid, gender from users_stream where gender='MALE';

PERSISTED QUERIES [CREATE STREAM AS ] results written to Kafka
Will be runnign automatically, need to use TERMINATE command to stop them

CREATE STREAM users_female AS SELECT userid AS userid, regionid FROM users_stream where gender='FEMALE';

CREATE STREAM users_male AS SELECT userid AS userid, regionid FROM users_stream where gender='MALE';


 CREATE STREAM pageviews_stream (userid varchar, pageid varchar) WITH (kafka_topic='pageviews', value_format='AVRO');
 
 select * from pageviews_stream;

JOIN

CREATE STREAM user_pageviews_enriched_stream AS SELECT users_stream.userid AS userid, pageid, regionid, gender FROM pageviews_stream LEFT JOIN users_stream WITHIN 1 HOURS ON pageviews_stream.userid = users_stream.userid;

select * from user_pageviews_enriched_stream;

Ctrl +C to exit

CREATE TABLE pageviews_region_table WITH (VALUE_FORMAT='AVRO') AS SELECT gender, regionid, COUNT() AS numusers FROM user_pageviews_enriched_stream WINDOW TUMBLING (size 60 second) GROUP BY gender, regionid HAVING COUNT() >= 1;

select * from pageviews_region_table;


kafka-avro-console-consumer --bootstrap-server k5.nodesense.ai:9092 --topic USERS_FEMALE --from-beginning --property schema.registry.url="http://k5.nodesense.ai:8081"

kafka-avro-console-consumer --bootstrap-server k5.nodesense.ai:9092 --topic PAGEVIEWS_REGION_TABLE --from-beginning --property schema.registry.url="http://k5.nodesense.ai:8081"


List the persisted queries

SHOW QUERIES;

List of queries with Query ID



C***** - QUERY ID

EXPLAIN CTAS_PAGEVIEWS_REGION_TABLE_3; 


To stop the query / once stopped, cannot be restarted, need to run fresh query

TERMINATE  CTAS_PAGEVIEWS_REGION_TABLE_3;

DROP STREAM  users_male; 


DROP TABLE  pageviews_region;

[FIXME: Creating new string after deleting them]


[Example for Invoices]
 
CREATE STREAM invoices_stream (id varchar, qty int, amount int, customerId varchar, state varchar, country varchar) WITH (kafka_topic='invoices', value_format='AVRO');

SELECT * FRom invoices_stream;

CREATE TABLE invoices_state_count WITH (VALUE_FORMAT='AVRO') AS SELECT state,  COUNT() AS numorders FROM invoices_stream WINDOW TUMBLING (size 60 second) GROUP BY state  HAVING COUNT() >= 1;
SELECT * FRom invoices_state_count;


Ctrl +C to exit



http://k5.nodesense.ai:50070/explorer.html#/

hdfs-sink.properties

name=hdfs-sink
connector.class=io.confluent.connect.hdfs.HdfsSinkConnector
tasks.max=1
topics=greetings
hdfs.url=hdfs://localhost:9000
flush.size=3
key.converter=org.apache.kafka.connect.storage.StringConverter
value.converter=org.apache.kafka.connect.storage.StringConverter

-----





kafka-consumer-groups --bootstrap-server k5.nodesense.ai:9092 --list

kafka-consumer-groups --bootstrap-server k5.nodesense.ai:9092 --describe --group invoice-consumer-example

with active members if any

kafka-consumer-groups --bootstrap-server k5.nodesense.ai:9092 --describe --group invoice-consumer-example --members

 --state [assignment strategy, round robin, range]
 
 kafka-consumer-groups --bootstrap-server k5.nodesense.ai:9092 --describe --group invoice-consumer-example --state 


delete consumer group --group my-other-group1 --group my-other-group2

kafka-consumer-groups --bootstrap-server localhost:9092 --delete --group  --group invoice-consumer-example
 

to reset offsets of a consumer group to the latest offset

kafka-consumer-groups.sh --bootstrap-server localhost:9092 --reset-offsets --group consumergroup1 --topic topic1 --to-latest

