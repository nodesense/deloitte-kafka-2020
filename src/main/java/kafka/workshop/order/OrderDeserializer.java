package kafka.workshop.order;


import java.nio.charset.StandardCharsets;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.apache.kafka.clients.consumer.ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG;


// Convert the serialized json bytes   to order Object
// Consumer
// consumer.poll().. pull kafka msg, then convert the content to order object
public class OrderDeserializer implements Deserializer<Order> {
    private ObjectMapper objectMapper = new ObjectMapper();

    private Class<Order> tClass = Order.class;

    /**
     * Default constructor needed by Kafka
     */
    public OrderDeserializer() {
        System.out.println("OrderConfirmationDeserializer created");
    }

    // called by consumer during startup
    @SuppressWarnings("unchecked")
    @Override
    public void configure(Map<String, ?> props, boolean isKey) {
        System.out.println("PRops are " + props);
        tClass = (Class<Order>) props.get(VALUE_DESERIALIZER_CLASS_CONFIG);
    }

    // invoked autoamtically by consumer during consumer.poll()
    // kafka consumer client will get bytes from broker
    // invoke deserialize passing bytes as input
    // deserialize converts bytes to Java Object [JSON to Object]
    @Override
    public Order deserialize(String topic, byte[] bytes) {
        System.out.println("orderJsonDeserializer deserialize called ");
        if (bytes == null)
            return null;

        Order order = null;
        try {
            System.out.println("Bytes received " +  new String(bytes, StandardCharsets.UTF_8));

            String o = new String(bytes, StandardCharsets.UTF_8).trim();
            System.out.println("Clean data " + o);

            // convert bytes to order Object
            // JSON.parse() - JS Equvalent
            order = objectMapper.readValue(o.getBytes(), Order.class);
        } catch (Exception e) {
            System.out.println("Error while parsing ");

            //throw new SerializationException(e);
        }

        return order;
    }

    @Override
    public void close() {

    }
}