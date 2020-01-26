package kafka.workshop;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import kafka.workshop.invoice.InvoiceProducer;
import kafka.workshop.models.Invoice;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;
import org.apache.commons.io.IOUtils;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import java.io.FileInputStream;
import java.util.Map;
import java.util.Properties;

import static org.apache.kafka.clients.producer.ProducerConfig.*;
import static org.apache.kafka.clients.producer.ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG;

public class ExampleSerializer {
    public static String SCHEMA_REGISTRY = "http://192.168.0.133:8081"; //default
    public static String TOPIC = "data-value-events";
    public static String BOOTSTRAP_SERVERS = "192.168.0.133:9092";

    public static void main(String args[]) throws  Exception {
        Properties props = new Properties();
        props.put("schema.registry.url", SCHEMA_REGISTRY);

        KafkaAvroSerializer avroSerializer = new KafkaAvroSerializer();

        Map<String, String> map = (Map)props;


        avroSerializer.configure(map, false);

        Invoice invoice = InvoiceProducer.getNextRandomInvoice();
        GenericRecord record = invoice;

        byte[] data = avroSerializer.serialize("invoices2", record);

        System.out.println(data.length);

        String schemaContent = null;

            String schemaPath = "/Users/krish/workshop/src/main/resources/avro/data-value-events.avsc";

        FileInputStream inputStream = new FileInputStream(schemaPath);
        try {
            schemaContent = IOUtils.toString(inputStream);
        } finally {
            inputStream.close();
        }

        Schema schema = new Schema.Parser().parse(schemaContent);



        String json = "{\"device_id\":\"SCBAS002SUV001\",\"name\":\"AUTO_MANUAL_SELECTION\",\"value\":0.0,\"quality\":0,\"status\":0,\"timestamp\":1579763969500,\"tag\":\"SCBAS002SUV001.AUTO_MANUAL_SELECTION\",\"station0\":\"BASSI\",\"station1\":\"BAS\",\"source\":\"HDA\",\"device_type\":\"SUV\"}";

        JsonNode datum = new ObjectMapper().readTree(json);
        Object avro =  kafka.workshop.jasvorno.JasvornoConverter.convertToAvro(datum, schema);

        GenericRecord record2 = (GenericRecord) avro;
        System.out.println("Avro " + record2);


        byte[] data2 = avroSerializer.serialize("dummy", record2);

        System.out.println(data2.length);


        Properties props2 = new Properties();

        props2.put(BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS); // broker address

        props2.put(ACKS_CONFIG, "0"); // acknowledge level "0", "1", "all"

        props2.put(RETRIES_CONFIG, 2); // how many retry when msg failed to send

        // whatever first condition reached,
        // group messages by max byte size, 16 KB, dispatch when it reaches 16 KB
        props2.put(BATCH_SIZE_CONFIG, 16000); // bytes
        // group the messages by max wait time, when 100 ms reached, dispatch the message
        props2.put(LINGER_MS_CONFIG, 100); // milli second

        // Reserved memory, pre-alloted in bytes
        props2.put(BUFFER_MEMORY_CONFIG, 33554432);
        props2.put("schema.registry.url", SCHEMA_REGISTRY);

        // Key/Value
        // Key is string, converted to byte array [serialized data]
        // Value is string, converted to byte array [serialized data]
        props2.put(KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        props2.put(VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.ByteArraySerializer");
       // props2.put("value.serializer", "io.confluent.kafka.serializers.KafkaAvroSerializer");

        // props.put("partitioner.class", CustomPartitioner.class);


        System.out.println("PRoducer Setup ");
        // Key as string, value as string
        Producer<String, byte[]> producer = new KafkaProducer<>(props2);

        String key = "Message"  ;
          ProducerRecord producerRecord = new ProducerRecord<>(TOPIC, key,data2);
        // producer.send(record); // async, non-blocking
        RecordMetadata metadata = (RecordMetadata) producer.send(producerRecord).get(); // sync, blocking

        System.out.println(metadata);
    }
}
