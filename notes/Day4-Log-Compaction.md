
Log Compaction

    
    log.segment.bytes=1073741824
    
    00000000000000000.log -- segment, max size shal be 1073741824/1 GB
    
    Max size reached?
    
    00000000032323200.log - this will grow to another 1 GB
    
    Max size reached?
        
        0000000004323232200.log - this will grow to another 1 GB
    
1 GB

retention policy - 1 day - 3:22, kafka decided to remove message from topic?
                                Kakfa will not delete entry on the same file..
                                

 data1,           data2, ....    data100, data200.......................... [750 MB] + new entry appended - 1 GB - file is completed
 22/jan/15:00      15:01                        22/jan/16:00
                      [250 MB]
                    ^
                    Delete retention point
                    
Compaction
        Out of 1 GB, we know that 250 MB is not needed, but we need retain 750 MB
        
        Leave the entries upto data2/250 MB
        create a new file 0000000678323232.log, move the content from data3.... till 1 GB [750 MB]
        
        Two Types
            Delete the old entry
            Keep the unique keys
            
  Delete Means all old data shall be removed 
        
  kafka-topics --zookeeper localhost:2181 --alter --topic my-topic --config   cleanup.policy=delete


  Compact Means all last known unique values retained forever 
        
  kafka-topics --zookeeper localhost:2181 --alter --topic my-topic --config   cleanup.policy=compact
  
