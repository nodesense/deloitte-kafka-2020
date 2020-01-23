
## Replicators

# Data Center Backup
    Data in One Kafka Cluster,
        move to another kafka cluster for backup
        
    System 1
        Existing Cluster [Kafka running in production]
        
        
    
    System 2
        Backup
        

    consumer
        consumer read from source/System 1
    producer
        producer write to destination/System2
    replicator
        A kafka connect, co-ordinate consumer and producer
        
        
    Hands-on??
        K5 - source
        K10 - destination
         
    consumer.properties
    producer.propeties
    replicator.properties
    
    > mkdir demo
    > cd demo
    demo > touch consumer.properties
    demo > touch producer.properties
    demo > touch replicator.properties
    demo > run replicator command


    nano  consumer.properties   [source kafka]

     
    bootstrap.servers=k5.nodesense.ai:9092

    nano producer.properties  [destination kafka]
    
    paste below content
    
    bootstrap.servers=k11.nodesense.ai:9092

    nano replicator.properties  [kafka connect]
    
    paste below content
    
rest.port=9083
topic.rename.format=${topic}.replica
replication.factor=1
config.storage.replication.factor=1
offset.storage.replication.factor=1
status.storage.replication.factor=1
confluent.topic.replication.factor=1
 

run on demo folder

replicator --cluster.id replicator --consumer.config  consumer.properties --producer.config  producer.properties --replication.config replicator.properties --whitelist 'greetings,invoice,words'


check on destination machine

kafka-console-consumer --bootstrap-server k11.nodesense.ai:9092 --topic greetings.replica  --from-beginning --property print.key=true
