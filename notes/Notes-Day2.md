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
                
    