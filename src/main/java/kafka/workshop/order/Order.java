package kafka.workshop.order;

// POJO class
// how to serialize to json
// how to deserialize using Kafka



import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import java.io.IOException;

public class Order {
    public String orderId;
    public Double amount;
    public String customerId;
    public String country;

    private static ObjectMapper objectMapper = new ObjectMapper();

    // seriazlize object to JSON text
    public String toJSON() throws IOException {
        return  objectMapper.writeValueAsString(this);
    }

    // deserializer Json text into Java Object
    public static Order fromJson(String json) throws  IOException {
        return objectMapper.readValue(json, Order.class);
    }
}