package kafka.workshop.order;

// invoked at producer side to decide partition numbers

import org.apache.kafka.common.PartitionInfo;

import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.kafka.clients.producer.Partitioner;
import org.apache.kafka.common.Cluster;
import org.apache.kafka.common.PartitionInfo;


public class OrderPartitioner implements Partitioner {
    private Random random;

    public OrderPartitioner() {
        System.out.println("OrderConfirmationPartitioner created");
        random = new Random();
    }

    // called during initialization of producer, props are passed as input here
    @Override
    public void configure(Map<String, ?> props) {
        // config properties
    }

    // TOPIC has max partition as 3
    // return value form this method should be either 0, 1, 2

    // method to decide which partition the message should go
    // who call this method and when?
    // producer.send() calls this method, after serialization
    // return a partition number (if 3 max partition, returns 0, 1, 2
    @Override
    public int partition(String topic,
                         Object key,  // key object as ref, String/POJO object
                         byte[] keyBytes, // key in serialized bytes
                         Object value,  // Order  object/POJO java object
                         byte[] valueBytes, // serialized json bytes
                         Cluster cluster) {

        int partition = 0;

        System.out.println("Partition Thread ID " + Thread.currentThread().getId());


        // cluster is useful to know max partition for topic
        List<PartitionInfo> partitions = cluster.partitionsForTopic(topic);

        int numPartitions = partitions.size(); // MAX partition for that topic

        System.out.println("Total partitions " + partitions.size() + " for topic");

        // USER CODE, custom code
        if (numPartitions <= 1) {
            return 0;
        }

        String country = (String) key;

        if (country.equals(("IN"))) {
            partition = 0;
        } else if (country.equals(("USA"))) {
            partition =  1;
        } else {
            partition = 2;
        }



        // OR

        // Find the id of current user based on the username
//        //Integer orderId = orderService.findUserId(userName);
//        Integer userId = random.nextInt(5);
//        // If the userId not found, default partition is 0
//        if (userId != null) {
//            partition = userId;
//        }


        // Other option, use murmur2 algorithm
        // or use hash key
        // -1 does ensure that 0 is not taken
        //  partition = Math.abs(Utils.murmur2(country.getBytes()) % (numPartitions - 1)) + 1;

        // Use custom models like Sensor key, product id etc for partition

        System.out.println(" For key " + key + " Part " + partition);
        return partition;
    }

    @Override
    public void close() {

    }

}