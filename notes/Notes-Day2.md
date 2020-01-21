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
        
        