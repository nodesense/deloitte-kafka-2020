Elements/Components of Kafka

     Broker
         Accept request from producers
         Store the messages in topic partitions
         Serve the messsages to consumers
         
     ZooKeeper
         Co-ordination service for brokers
        
     Topics
        Paritions starting partition 0, ..
        
     Producer decides where/which partition the message should go?
        Round Robin -- When the key is null
        Hash % num-partitions -- when is not null
        Custom Partitioner - Developer code
        
        A producer can publish to one or more topics
        Producer basically push the data to broker
        
        Serialization:
            for Key/Value
            Convert the data [Plain Text/JSON/XML/int/float/custom formats] into byte format
            
     Consumer
        Pull data from the broker
        Consumer can consume data  from many topics/parititions
        
        Deserialization:
            for key/value
            Convert the bytes reiceved from Broker into data/object [XML/JSON/Java Object]
            
     Testing commands
     
     kafka-topics - create/list/describe/delete/alter the topics
     kafka-console-producer
     kafka-console-consumer
     
     Java Project
        Maven
        SimpleConsumer
        SimpleProducer
        
        
 Producers
    Producer ---> Broker0, Broker1, Broker2, Broker3
    
    Topic - 1 replication 
    Producer writes to Broker 
             Broker is not available /system failure/HDD/network problem
    
    Acknowledge, producer except from Broker whether message is safely written or not
    
    Ack - "0"
        - Broker received the message from the producer
        - Not written to File/HDD/Not Persisted
        - Send Ack to Producer
        
        Cons
            - If broker HDD Fails/System restarted before writing content into file system
             we loose the message
        
        Pros
            - Super Fast
            
    Ack - "1"
         - Broker received the message from the producer
         - Written to its own File system/HDD/Persisted
         - Send Ack to Producer
         
         Cons
            - What happen the system itself corrupt, HDD failure, we cannot recover
            - Doesn't replicate the message in another system
         
         Pros
            -- Relatively fast, but slowre than Ack 0, data is persisted
            
    Ack - "all"
        - Remember replication? Having the same copy of the message in another system
        - Broker received the message from the producer
        - Written to its own File system/HDD/Persisted
        - Ensure that other replicas replicated the message
        - Send Ack to Producer
        
        Pros
            data is safely stored
            
        Cons
            - Comparatively slow
            
Producer - Java SDK
    - Async
    - Producer uses worker thread to send the messages to Broker
    producer.send(record) - add to queue/buffer with in producer program
    When the data shall be send to producer?
    BATCH_SIZE_CONFIG - Maximum byte size of the batch - 16000 bytes
             
    Example:
        producer send message /queued - 4000 bytes [not send] - 4000
        producer send message /queued - 4000 bytes [not send] - 8000
        producer send message /queued - 4000 bytes [not send] - 12000
        producer send message /queued - 4000 bytes  - 16000  [Reached Max batch size]
        
        Now Producer SEND the messages to Broker in single attempt
        Here it can compress the data

    LINGER_MS_CONFIG - Value in milli seconds - 1000 ms / 1 sec
    
        [09:55] producer send message /queued - 4000 bytes [send at 09:56] - 4000
        [10:00 AM] producer send message /queued - 4000 bytes [send at 10:01] - 4000
        [10:05 AM] producer send message /queued - 4000 bytes [send at 10:05] - 4000
        [10:10 AM] producer send message /queued - 4000 bytes  [send at 10:11] - 4000  [Reached Max batch size]
            
            
    LINGER_MS_CONFIG - Value in milli seconds - 1000 ms / 5 sec
    
        [09:55] producer send message /queued - 4000 bytes   - 4000
        [09:56] producer send message /queued - 4000 bytes   - 8000
        [09:57] producer send message /queued - 4000 bytes   - 12000
        [09:58] producer send message /queued - 4000 bytes   - 16000 [Max size reached] - msg shall be send

         
    Kafka producer keep the message in buffer either BATCH_SIZE_CONFIG arrives or LINGER_MS_CONFIG arrives
    
    
Consumer Group
    Started single consumer 
        Consumer 1    -- Partition 0, 1, 2 allocated to this consumer
    Start the second consumer, partitions can be split amoung consumers
        Consumer 1 - P0, P1
        Consumer 2 - P2
        
    Start the thrid consumer, partitions can be split amoung consumers
            Consumer 1 - P0
            Consumer 2 - P2
            Consumer 3 - P1
            
     Start the forth consumer, partitions can be split amoung consumers
                Consumer 1 - P0
                Consumer 2 - P2
                Consumer 3 - P1
                Consumer 4 - ?? no partition left/IDLE
                
                
    Consumer Group Offset and Offset commits
        __consumer_offsets - automatically created with in kafka- 50 partitions
                __consumer_offsets contains information about consumer group and last committed offset
                
        GROUP_ID_CONFIG = "greetings-consumer-group"/commit offset
            "greetings-consumer-group" {
                 partition-0 - 549 [commited by consumer]
                 parition-1 - 461 [by whom? consumer commit offet to broker]
                 partition-2 - 522 [commited by consumer]
            }
                
    Async/Sync
    
    Sync - Blocing call, block current thread until it is executed/completed
    Async - Non-Blocking call, doesn't block the current thread, current thread can continue processing other messages
            - Async has the callback
            
            
    function Thread/run() {
       consumeMessage() - 1 sec
       processMessage() - 5 sec, 
       commitOffsetAsync(callback) - handled by worker thread - 0 second
    }
    
    workerThread() {
           commmitOffset() - 2 second
    }
    
CLUSTER SETUP
  -- 4 Brokers cluster + 1 zookeeper [for demonstration, use the same system with differnt port]
  -- Broker id 0 - Port 9092, log dir /tmp/kafka-logs
  -- Broker id 1 - Port 9092, log dir /tmp/kafka-logs-1
  -- Broker id 2 - Port 9093, log dir /tmp/kafka-logs-2
  -- Broker id 3 - port 9094, log dir /tmp/kafka-logs-3
        
%KAFKA_HOME%/etc/kafka/server.properties [COPY/PASTE this file 3 times]
%KAFKA_HOME%/etc/kafka/server1.properties
%KAFKA_HOME%/etc/kafka/server2.properties
%KAFKA_HOME%/etc/kafka/server3.properties

For Windows
    This may be already started
    
    kafka-server-start.bat %KAFKA_HOME%\etc\kafka\server.properties
    
    next command prompt
        kafka-server-start.bat %KAFKA_HOME%\etc\kafka\server1.properties

    next command prompt
        kafka-server-start.bat %KAFKA_HOME%\etc\kafka\server2.properties

    next command prompt
        kafka-server-start.bat %KAFKA_HOME%\etc\kafka\server3.properties



for mac/linux/unix
    kafka-server-start $KAFKA_HOME/etc/kafka/server.properties
    kafka-server-start $KAFKA_HOME/etc/kafka/server1.properties
    kafka-server-start $KAFKA_HOME/etc/kafka/server2.properties
    kafka-server-start $KAFKA_HOME/etc/kafka/server3.properties

Check the cluster up and running using ZooKeeper

    open new command prompt
    
    zookeeper-shell localhost:2181
    
    above shall bring a cli for zookeeper/tree strucutred configuration
    
    run below command in zookeeper shell
    
    ls /
    ls /brokers
    ls /brokers/ids
    ls /brokers/topics
    
CREATE A TOPIC WITH 3 Replications and 3 partitions
    topic name: texts
    partitions: 3
    replications: 3
    Brokers: 4
    
    kafka-topics --create --zookeeper localhost:2181 --replication-factor 3 --partitions 3 --topic texts
    kafka-topics --describe  --zookeeper localhost:2181 --topic texts
    
kafka-topics --describe  --zookeeper localhost:2181 --topic texts
Topic:texts	PartitionCount:3	ReplicationFactor:3	Configs:
	Topic: texts	Partition: 0	Leader: 0 [Broker-id]	Replicas: 0,2,3 [Broker-ids]	Isr: 0,2,3 [Broker-ids]
	Topic: texts	Partition: 1	Leader: 1 [Broker-id]	Replicas: 1,3,0	Isr: 1,3,0
	Topic: texts	Partition: 2	Leader: 2 [Broker-id]	Replicas: 2,0,1	Isr: 2,0,1
	
ISR - In Sync Replicas
    Broker 0 - Lead for PArtition 0 
                All writes for partition 0 goes to Broker 0
                Ack is processed here
    Broker 1 - Lead for parition 1
               All writes for partition 1 goes to Broker 1
                Ack is processed here
    
    Broker 2 - Lead for parition 2
               All writes for partition 2 goes to Broker 2
                Ack is processed here
                
Partition 0 - Data for partition 0 kept by Brokers  0,2,3  as replicas
Partition 1 - Data for partition 1 kept by Brokers  1,3,0  as replicas
Partition 2 - Data for partition 2 kept by Brokers  2,0,1 as replicas


ISR 
    Leader P0 - writen to its own disk
    But other replicas are slow, they need time to copy the data
        Slower replica may not be sync with leader
    

Install putty [ssh]  OR "git for windows"

######

Confluent Kafka on LINUX - Enterprise

confluent start
confluent status 
confluent stop 

Kx.nodesense.ai 
        ZooKeeper - 2181
        Broker - 9092
        Schema Registry - 8081
        Control Center UI - 9021
        
Open the browser http://Kx.nodesense.ai:9021

Create a topic using ZooKeeper


Below can be executed from windows machine also

    kafka-topics --create --zookeeper k5.nodesense.ai:2181 --replication-factor 1 --partitions 3 --topic texts
    
    kafka-topics --describe  --zookeeper k5.nodesense.ai:2181 --topic texts
    
    
# AVRO

JSON
{
"orderNo":41243243432,
"amount":434.32,
"customerId":42343432423,
"date":"2020 Jan, 22, 10:00:00 AM"
}

JSON 
    NO Schema
    Data is represenated as String/TEXT/CHAR/Unicode

Total CHARs are 102 x 2 = 204 bytes per record
Char is unicode - 2 bytes 
-----


Producer Transfer 204 bytes to Broker
Broker stores 204 bytes in HDD
Update REplicas with 204
Consumer, read 204 bytes



AVRO

    Data serialization system 
    serialization and deserialization of the data
    Schema
    Encoding in Binary format

{
"orderNo": int , 4 bytes
"amount": float , 4 bytes
"customerId": int , 4 bytes
"date": int8 - 8 bytes
} = Total Bytes = 20 bytes

{
 41243243432 [index 0]
 434.32 [index 1]
 42343432423, [index 2]
 1579603677153 [index 3]
} == 20 bytes


Producer Transfer 20 bytes to Broker
    serialization will be super fast compared to JSON
Broker stores 20 bytes in HDD
Update REplicas with 20
Consumer, read 20 bytes
        convert to object will be  super fast than JSON


Storage Cost
Retrival/Storage time with HDD /SSD/HDD/SAS/NAS.. - latency
NEtwork - Brokers replicas, producer/consumers
Schema - Data Format/Validation



Apache NIFI / Embed the schema inside message
    {{
        schema as whole
    }}
    {{data}}

Apache Kafka / Doesn't embed schema with message, instead include schema version number
    {{schema-version}} - 4 bytes number
    {{data}}

Where is schema stored?
    Schema registry tool - to store the schemas with version
    HTTP REST Service running on port 8081
    
    Where eactly the schemas are stored?
    
    Ans: ZooKeeper
    
KAFKA for batch? Why?
   Data Pipeline, persisted messages/streams
   
   Whenever data changed,
   Pull data from SQL DB/CSV/FTP/other sources
   store into Redis/Mem Cached
   store the changed data into Elastics search
   Store the data into Hadoop
   
   
   
 Downlaod  and store into lib folder of project
   http://apachemirror.wuchna.com/avro/stable/java/avro-1.9.1.jar
   
   http://apachemirror.wuchna.com/avro/stable/java/avro-tools-1.9.1.jar
   
 Command to generate POJO class from the schema
 
 java -jar ./lib/avro-tools-1.9.1.jar compile schema ./src/main/resources/avro/invoice.avsc ./src/main/java


you can add  to pom.xml

  <dependency>
            <groupId>org.apache.avro</groupId>
            <artifactId>avro</artifactId>
            <version>1.9.1</version>
  </dependency>
  
  
  
SCHEMA REGISTRY/REST API

http://k5.nodesense.ai:8081/

To know all schemas,

http://k5.nodesense.ai:8081/subjects

http://k5.nodesense.ai:8081/subjects/invoices-key/versions

http://k5.nodesense.ai:8081/subjects/invoices-key/versions/1


http://k5.nodesense.ai:8081/subjects/invoices-value/versions

http://k5.nodesense.ai:8081/subjects/invoices-value/versions/1


    kafka-console-consumer --bootstrap-server k5.nodesense.ai:9092 --topic invoices --from-beginning
    
To view Avro Formatted data
    kafka-avro-console-consumer --bootstrap-server k5.nodesense.ai:9092 --topic invoices --from-beginning

