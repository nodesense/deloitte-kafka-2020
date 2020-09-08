package kafka.workshop.order;


import kafka.workshop.Settings;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Properties;
import java.util.Random;

import static org.apache.kafka.clients.producer.ProducerConfig.*;
import static org.apache.kafka.clients.producer.ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG;

// kafka-topics --zookeeper k5.nodesense.ai:2181 --create --topic orders --replication-factor 1 --partitions 3

public class OrderProducer {

     public static String TOPIC = "orders";

    public static String countries[] = new String[] {"IN", "USA", "EU", "AU", "DE"};

    static Random r = new Random();

    static Order nextOrder() {
        Order order = new Order();
        order.amount = 100.0 + r.nextInt(1000);
        order.orderId = String.valueOf(r.nextInt(1000000));
        order.customerId = String.valueOf(r.nextInt(100));
        order.country = countries[r.nextInt(countries.length)];

        return order;
    }

    public static void main(String[] args) throws  Exception {
        System.out.println("Welcome to producer");

        Properties props = new Properties();

        props.put(BOOTSTRAP_SERVERS_CONFIG, Settings.BOOTSTRAP_SERVERS);
        props.put(ACKS_CONFIG, "all");
        props.put(RETRIES_CONFIG, 0);
        props.put(BATCH_SIZE_CONFIG, 16000);
        props.put(LINGER_MS_CONFIG, 100);
        props.put(BUFFER_MEMORY_CONFIG, 33554432);

        props.put(KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        props.put(VALUE_SERIALIZER_CLASS_CONFIG, OrderSerializer.class);

        //props.put("partitioner.class", "kafka.workshop.order.OrderPartitioner");
        props.put("partitioner.class", OrderPartitioner.class);


        // Key as string, value as OrderConfirmation
        Producer<String, Order> producer = new KafkaProducer<>(props);


        Random r = new Random();

        int counter = 10;
        for (int i = 0 ; i < 10 ;i++) {
            Order order = nextOrder();
            // producer record, topic, key (null), value (message)
            // send message, not waiting for ack

            String key = order.country;

            ProducerRecord<String, Order> record = new ProducerRecord<>(TOPIC, key, order);
            System.out.println("Sending " + order.orderId);

            // produer.send will invoke serialize method internally
            // pass topic name and order object parameter
            // serialize will return bytes as output
            // producer shall call custom partitioner's partition method to know the partition
            // bytes shall be send to broker
            producer.send(record);

            System.out.printf("order send %s sent\n", record);
            Thread.sleep(5000); // Demo only,
        }

        producer.close();


    }


}