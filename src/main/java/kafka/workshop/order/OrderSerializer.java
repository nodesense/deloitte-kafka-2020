package kafka.workshop.order;

import java.nio.charset.StandardCharsets;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Serializer;

import java.nio.charset.StandardCharsets;
import java.util.Map;

// Convert OrderConfirmation object (java object) to serialized format/JSON bytes
// called by the producer, when producer.send(.., orderConfirmation)
public class OrderSerializer<T> implements Serializer<T> {
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Default constructor needed by Kafka
     */
    public OrderSerializer() {
        System.out.println("OrderSerializer object created ");
    }

    // called one time by the Producer to initialize your serializer
    @Override
    public void configure(Map<String, ?> props, boolean isKey) {
    }

    // invoked when producer.send(order)
    //orderObj is POJO object represent order
    @Override
    public byte[] serialize(String topic, T order) {
        System.out.println("Orderconfirmation serialize called ");

        if (order == null)
            return null;

        try {
            // convert order to bytes // JS JSON.stringfy().toBytes()
            byte[] bytes = objectMapper.writeValueAsBytes(order);
            System.out.println("Bytes " + bytes);

            System.out.println("Bytes string " +  new String(bytes, StandardCharsets.UTF_8));
            return bytes;

        } catch (Exception e) {
            throw new SerializationException("Error serializing JSON message", e);
        }
    }

    // is invoked by the producer when producer.close is called, basically to clean up the resources
    @Override
    public void close() {
    }
}