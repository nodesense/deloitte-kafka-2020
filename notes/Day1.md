
Topic - Invoices

Producer produces below meessages on topic Invoices

[Invoice1, Invoice2, Invoice3,............Invoice100]

Brokers
    Invoices Topic
    Broker parition the data - subset of the data
    by default, 1 partition created per topic
    we can go as many partitions possible
    Each partition has id/index starting from 0

Example: Invoices Topic, Max Parition is 1
    Partition 0 - [
                    Invoice1,
                    Invoice2, 
                    Invoice3,
                    ...
                    Invoice100
                  ]


Example: Invoices Topic, Max Partition is 2
    partition is subset within topic

    Partition 0 - [
                    Invoice1,
                    Invoice3,
                    ...
                    Invoice 99
                    ] 
    Partition 1 - [
                    Invoice2,
                    Invoice4,
                    ..
                    Invoice 100
                  ] 


Example: Invoices Topic, Max Partition is 3
    partition is subset within topic

    Partition 0 - [
                    Invoice1, /Offset no 0
                    Invoice4, /offset no 1
                    ...
                    Invoice ../ /Offset no n
                    ] 
    Partition 1 - [
                    Invoice2, / Offset no 0, 
                    Invoice5, / Offset no 1
                    ..
                    Invoice ..
                  ] 
Partition 2 - [
                    Invoice3,
                    Invoice6,
                    ..
                    Invoice ...
                  ] 

How the data stored in kafka?

    Stored in Flat file, append only log file
        WAL - Write Ahead Log,
            open the file in append mode
            append the data end of file file

    Directory names will be <<topic-name>>-<<partition-id>>
        files are stored inside directory
    Directories [assuming 3 partitions]
            invoices-0
                        0000000000000000.log -- all data [message/key/value]
                        0000000000000000.index [offset to log file
                                                offet 0 stored in log file at address 0,
                                                offset 1 stored in lgo file at addresss 100]
                        0000000000000000.timeindex - stored time
                                               [timeindex to offset,
                                                Monday, 20, 01, 2019, 11:00 AM, Offset 1
                                               ]
            invoices-1
                        0000000000000000.index
                        0000000000000000.log
            invoices-2
                        0000000000000000.index
                        0000000000000000.log


----
Single Broker 0, 1 topic, 2 parititions
    Broker 0 
        invoices topic
            parition 0 : [Inv1, Inv3, invoce 5...]
            partition 1: [inv2, inv4....]

        Folders
            invoices-0/ 
                    000000.log, etc
            invoices-1/
                    0000000.log


Two Brokers [Broker 0, Broker 1], 
                            1 topic, 2 parititions
    Broker 0  - system 1
        invoices topic
            partition 1: [inv2, inv4....]

        Folders
            invoices-1/
                    0000000.log

    Broker 1  - system 1
        invoices topic
            partition 0: [inv1, inv3....]

        Folders
            invoices-0/
                    0000000.log


Two Brokers [Broker 0, Broker 1], 
                            1 topic, 3 parititions
    Broker 0  - system 1
        invoices topic
            partition 1: [inv2, inv4....]
            partition 2: [inv10, inv20....]
        Folders
            invoices-1/
                    0000000.log
            invoices-2/
                    0000000.log
    Broker 1  - system 1
        invoices topic
            partition 0: [inv1, inv3....]

        Folders
            invoices-0/
                    0000000.log

Producer
    one decides the partition number for a message
    how it decides? based on messsage key

-- How partition id is decided
    -- by using key part of the message
    -- by producer

        Message [Key: IN, value: {order_id: 432143,amount: 456}]

        getHashKey("IN") -- 3232 % 2 (max paritions) = 0 [parition id]

        Message [Key: USA, value: {order_id: 76443,amount: 32}]

        getHashKey("USA") -- 32325 % 2 (max paritions) = 1 [parition id]

        Message [Key: IN, value: {order_id: 87432,amount: 1000}]

        getHashKey("IN") -- 3232 % 2 (max paritions) = 0 [parition id]


Retention Policy
    -- decide how long the data can be stored in the Kafka
    -- Topic 1 - 1 day [24 hours]
    -- Topic 2 - 3 days [72 hours]
    -- Topcic 3 - 14 days [default]

example:
    Topic name: Invoice
    max parition: 1
    Retention Policy: 24 hours
    Partition 0 : [
                    [Invoice-1, Offset 0] Sunday, Jan 19, 2020, at 12:15 PM - marked for delete
                    [Invoice-2, Offset 1] Sunday, Jan 19, 2020, at 14:15 PM
                    [Invoice-3, , Offset 2] Sunday, Jan 19, 2020, at 16:15 PM
                  ]

    Compaction - Discussed later - Algorithm
        to decide to keep/delete the messages from the topic/partition

        Scheduled - 5 minutes go and check for deletable messages
            12:17 - check for deletable messages
            It will do compaction, by moving to new file, leaving expired data

        Parition-0
            0000000000000.log [12:15 all data here, Inv1, Inv2, Inv3]
                    [compaction come in, need to clean up the message]

            000000000001.log [Inv2 /offset 1, Inv3/offset 2, inv4, offset 3.....]

            0000000000000.log shall be deleted from HDD
            

Replication
    Backup for the partition data
    if one broker fails, another broker shall have same copy of the data

    By default, only one copy of the partition is store
        by default replication is always 1

    1 Broker cluster
            Broker 0 - Max replication 1

    2 Brokers cluster - Max replication 2, Min Replcaition - 1
            Broker 0 - [Invoice-0]
            Broker 1 -  [Invoice-0]

        Topic Invoices, PArition 0 is replicated in Broker 1
    
    3 Brokers cluster - Max replication 3, Min Replcaition - 1
            Broker 0 - [Invoice-0]
            Broker 1 -  [Invoice-0]
            Broker 2 -  [Invoice-0]

    for healthy cluster,
         at least maintain 3 replicas or 5 for best one

    100 Brokers cluster - Max replication 100, Min Replcaition - 1
            Broker 0 - 
            Broker 1 -  [Invoice-0]
            Broker 2 -  
            Broker3 - [Invoice-0]
            Broker4
            ...
            Broker99 - [Invoice-0]

            IDEAL Replications can be 3 to 5


Consumer Group
    Group of Consumers is called Consumer Group, there will be UNIQUE NAME PER consumer group


10 producers/Order Producer
    150 orders per minute


Consumer: 
    SMS Gateway 1 gateway can handle upto 25 msg per minute
    consumer lagging 
        consume less messages than proceduced

Solution?

    4 number of sms gateways each can handle 25 msg per minute = 100 per minute

Consumer Group - "SMSConsumersGroup"
    SMS Gateway 1
    SMS Gateway 2
    SMS Gateway 3
    SMS Gateway 4
    
Consumer Group - "EmailConsumerGroup", 50 msg per minute
    Email Gateway 1
    Email Gateway 2

What is REALLY BAD, do not mix the consumer group
    BAD CONSUMER GROUP
        SMS Gateway 1
        Email Gateway 1

Partition and Consumer groups are related

Kafka decides and allocate partition to consumer in the consumer group
    What if 2 consumers read from same parition, same offet?

Orders Topic -  1 max partition
Parition 0 [ Order1, Order 2, Order 3....]

1. Initial Setup:
Consumer Group - "SMSConsumersGroup"
    SMS Gateway Consumer 1 - Parition 0 is allocated by Kafka
    SMS Gateway Consumer 2 - IDLE, will not get message

2. Consumer 1 died, kafka now allocate parition 0 to consumer 2
    SMS Gateway Consumer 1 - DEAD, no more
    SMS Gateway Consumer 2 - Parition 0 is allocated by Kafka

----


Orders Topic -  2 max partition
Parition 0 [ Order1, Order 3, Order 5....]
PArition 1 [Order2, Order 4, Order 6....]

1. Initial Setup:
Consumer Group - "SMSConsumersGroup"
    SMS Gateway Consumer 1 - Parition 0, PArition 1 is allocated by Kafka

2. Add second consumer: Kafka will do REBALANCE paritions to consumers
Consumer Group - "SMSConsumersGroup"
    1.a Kafka pull out both parition 0 and partition 1 from consumer 1
    2.b KAfka allocate patition 0 to Consumer 1
    2.c Kafka allocate parition 1 to consumer 2
    2.d Add 3rd Consumer 3 to the cluster
    2.e Kafka pull out  parition 0 from consumer 1 and partition 1 from consumer 2
    2.f Kafka rebalance the consumers and paritions
    2.g Consumer 1 is IDEL
        Consumer 1 has partition 0
        Consuemr 2 has parition 1
    

Installation

Extract confluent-5.2.2-2.11.tar.gz file into folder
Move confluent-5.2.2 into c:\confluent-5.2.2

NO SPACE BETWEEN PATH

c:\confluent-5.2.2
        bin
        etc
        lib

Windows Environemnt Variables

Press  your windows key
    search for environment
    open environment variable window
        User Environemnt Variable
            Add below variables 
                    JAVA_HOME
                    C:\Program Files\Java\jdk1.8.0_241

            another environemnt
                    KAFKA_HOME
                    c:\confluent-5.2.2

            Update the PATH VARIABLE [you may have existing PATH]
                       add
                        C:\Program Files\Java\jdk1.8.0_241\bin
                         
                        and add 
                        c:\confluent-5.2.2\bin\windows



Open command Prompt

    javac -version
    java -version


in the command prompt
For Windows

    zookeeper-server-start.bat %KAFKA_HOME%\etc\kafka\zookeeper.properties

For mac/linux/unix,
 zookeeper-server-start $KAFKA_HOME/etc/kafka/zookeeper.properties

ZooKeeper 
    Apache ZooKeeper
    Hadoop
    Distributed Co-ordination
        Maintain heart beat with all Brokers
        Maintain broker list [avaible ones]
        watch 
            if one broker fails, it notifies other brokers
    Meta - All Topics, partition meta information
    Schema -- all schema registry info stored here

Starting Kafka Broker
    single broker, ie no cluster
    each broker shall have unique id in number/integer starting from 0

second command prompt for windows
    kafka-server-start.bat %KAFKA_HOME%\etc\kafka\server.properties

for mac/linux/unix
    kafka-server-start $KAFKA_HOME/etc/kafka/server.properties

    port 9092
    logs stored in /tmp/kafka-logs


ZooKeeper - 2181 port
Kafka - 9092 port, heart beat with zookeeper


Create the topics
        kafka-topics 

Open new command prompt

        kafka-topics  --create --bootstrap-server localhost:9092 --replication-factor 1 --partitions 1 --topic test


        kafka-topics --list --bootstrap-server localhost:9092 test

OR 

        kafka-topics --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic test2

        kafka-topics --list --zookeeper localhost:2181

        kafka-topics --describe --zookeeper localhost:2181 --topic test



Producer to produce messages on the topic
    message - key/value, key can be null, 
    
    notes: enter some text and press enter key, each line is consider as one message

    kafka-console-producer --broker-list localhost:9092 --topic test


Consumer to consume the messages from topic

    note: open 4th Command Prompt

    listen for the messages published/latest

    kafka-console-consumer --bootstrap-server localhost:9092 --topic test


    get call the messages from beginging and then for new messages

    kafka-console-consumer --bootstrap-server localhost:9092 --topic test --from-beginning

from a specific partition only

kafka-console-consumer --bootstrap-server localhost:9092 --topic test --partition 0 --from-beginning


from a specific partition, from a specific offset onwards only

kafka-console-consumer --bootstrap-server localhost:9092 --topic test --partition 0 --offset 4

[TODO] - limiting top messsage

c:\tmp\kafka-logs

----

Our keys are null, so the data is stored in round robin basics on each p0, p1, p2...

kafka-topics --create --zookeeper localhost:2181 --replication-factor 1 --partitions 3 --topic messages

kafka-console-producer --broker-list localhost:9092 --topic messages

kafka-console-consumer --bootstrap-server localhost:9092 --topic messages --partition 0 --from-beginning


kafka-console-consumer --bootstrap-server localhost:9092 --topic messages --partition 1 --from-beginning

kafka-console-consumer --bootstrap-server localhost:9092 --topic messages --partition 2 --from-beginning
 

---

console producer with key:value, where as key is used for partitioning

kafka-topics --create --zookeeper localhost:2181 --replication-factor 1 --partitions 3 --topic greetings

kafka-console-producer --broker-list localhost:9092 --topic greetings --property "parse.key=true" --property "key.separator=:"

keyname:value
birthday: happy birthday
republic: happy republic day 1

kafka-console-consumer --bootstrap-server localhost:9092 --topic greetings --partition 0 --from-beginning --property print.key=true

kafka-console-consumer --bootstrap-server localhost:9092 --topic greetings --partition 1 --from-beginning

kafka-console-consumer --bootstrap-server localhost:9092 --topic greetings --partition 2 --from-beginning

---

IntelliJ

Create a New Project

Select Maven on left side
Ensure that Project SDK is 1.8, if not set, select the JDK Path

