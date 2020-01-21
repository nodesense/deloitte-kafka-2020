package kafka.workshop;

import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import java.util.*;

import static java.time.Duration.ofSeconds;
import static org.apache.kafka.clients.consumer.ConsumerConfig.*;
import static java.util.Collections.singletonList;


// kafka-topics --zookeeper localhost:2181 --create --topic texts --replication-factor 3 --partitions 3


// Key/Value consumer
// Auto commit disabled
public class RebalanceConsumer {

    public static String BOOTSTRAP_SERVERS = "localhost:9092";
    public static String TOPIC = "texts";

    public static void main(String[] args) {

        Properties props = new Properties();
        props.put(BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        props.put(GROUP_ID_CONFIG, "rebalance-consumer"); // offset, etc, TODO
        props.put(ENABLE_AUTO_COMMIT_CONFIG, "false");
        props.put(AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000");
        props.put(SESSION_TIMEOUT_MS_CONFIG, "30000");
        props.put(KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        props.put(VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");

        // <Key as string, Value as string>
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);



        List<PartitionInfo> partitions = consumer.partitionsFor(TOPIC);
        for (PartitionInfo partitionInfo: partitions) {
            System.out.println("Partition " + partitionInfo);

        }


        // Subscribe for topic(s)
        consumer.subscribe(singletonList(TOPIC), new ConsumerRebalanceListener() {
            public void onPartitionsRevoked(Collection<TopicPartition> partitions) {
                System.out.printf("%s topic-partitions are revoked from this consumer\n", Arrays.toString(partitions.toArray()));
            }

            public void onPartitionsAssigned(Collection<TopicPartition> partitions) {
                System.out.printf("%s topic-partitions are assigned to this consumer\n", Arrays.toString(partitions.toArray()));

                String POSITION = "lastCommitted"; // offset | begin | end | lastCommitted

                Iterator<TopicPartition> topicPartitionIterator = partitions.iterator();
                while(topicPartitionIterator.hasNext()) {
                    TopicPartition topicPartition = topicPartitionIterator.next();

                    System.out.println("Part No " + topicPartition.partition());

                    System.out.println("Current position is " +
                            consumer.position(topicPartition) +
                            " committed  is ->" +
                            consumer.committed(topicPartition) );


                    OffsetAndMetadata offetMeta = consumer.committed(topicPartition);



                    if (POSITION == "offset") {
                        // specific offset on a specific partitions assigned to this consumer
                        consumer.seek(topicPartition, 10);
                    }

                    if (POSITION == "begin") {
                        // start from offset 0 on a specific partitions assigned to this consumer
                        consumer.seekToBeginning(Arrays.asList(topicPartition));
                    }

                    if (POSITION == "end") {
                        // start from current (latest) on a specific partitions assigned to this consumer
                        consumer.seekToEnd(Arrays.asList(topicPartition));
                    }

                    if (POSITION == "lastCommitted") {
                        // start from current (latest) on a specific partitions assigned to this consumer
                        // last commited offset
                        ;
                        consumer.seek(topicPartition, offetMeta.offset());

                    }
                }
            }
        });

        System.out.println("Rebalance Consumer Starting!");

        while (true) {
            // poll (timeout value), wait for 1 second, get all the messages
            // <Key, Value>
            ConsumerRecords<String, String> records = consumer.poll(ofSeconds(1));
            // if no messages
            if (records.count() == 0)
                continue;

            // Iterating over each record
            for (ConsumerRecord<String, String> record : records) {
                System.out.printf("partition= %d, offset= %d, key= %s, value= %s\n",
                        record.partition(),
                        record.offset(),
                        record.key(),
                        record.value());
            }

            // send ack to broker, msg are processed
            consumer.commitSync();
        }
    }
}