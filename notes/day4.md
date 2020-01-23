# STREAMS


// Take pom.xml dependencies
// WordCountStream.java

kafka-topics --zookeeper k5.nodesense.ai:2181 --create --topic words --replication-factor 1 --partitions 3

kafka-console-producer --broker-list k5.nodesense.ai:9092 --topic words
    

Run the Java application
 
See the output but in binary


kafka-console-consumer --bootstrap-server k5.nodesense.ai:9092 --topic words-count-output --from-beginning --property print.key=true  --property value.deserializer=org.apache.kafka.common.serialization.LongDeserializer





Run the WordStrema Java application


---
Invoices Stream

Run the Invoice Producer

Run the InvoiceStream application

Run the consume consumer

kafka-console-consumer --bootstrap-server k5.nodesense.ai:9092 --topic statewise-invoices-count --from-beginning --property print.key=true  --property value.deserializer=org.apache.kafka.common.serialization.LongDeserializer

kafka-console-consumer --bootstrap-server k5.nodesense.ai:9092 --topic statewise-amount --from-beginning --property print.key=true  --property value.deserializer=org.apache.kafka.common.serialization.LongDeserializer

 






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
