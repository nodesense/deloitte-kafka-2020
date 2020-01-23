Retention 
    Means how the long the data can be stored in Kafka?
    
    Two Types
    
        Either By Time
        Or By  Size
        
        
    Time based Retention Policy
            server.properties for default one
                    log.retention.hours=168
                    
            or by topic configuration
            
            kafka-topics --zookeeper localhost:2181 --alter --topic my-topic --config retention.ms=1680000
            
            
    Size Based Retention if size reached 100 MB, start deleting old content
    
            kafka-topics --zookeeper localhost:2181 --alter --topic my-topic --config  retention.bytes=100000000


