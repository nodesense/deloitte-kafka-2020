package kafka.workshop.invoice;

import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import kafka.workshop.models.Invoice;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.*;
import scala.Tuple2;

import java.util.Collections;
import java.util.Map;


// kafka-topics --zookeeper localhost:2181 --create --topic statewise-invoices-count --replication-factor 1 --partitions 1

// kafka-console-consumer --bootstrap-server localhost:9092 --topic statewise-invoices-count --from-beginning --property print.key=true --property print.value=true --formatter kafka.tools.DefaultMessageFormatter --property value.deserializer=org.apache.kafka.common.serialization.LongDeserializer

import java.util.Properties;


import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.*;
import io.confluent.kafka.streams.serdes.avro.GenericAvroSerde;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import java.util.Properties;
import java.util.concurrent.TimeUnit;


public class InvoiceStream {
    static  String bootstrapServers = "k5.nodesense.ai:9092";
    //FIXME: chance schema url
    static String schemaUrl = "http://k5.nodesense.ai:8081";

    public static void main(String[] args) throws  Exception {
        final Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "product-invoice-stream");
        props.put(StreamsConfig.CLIENT_ID_CONFIG, "product-invoice-stream-client");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());

        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, SpecificAvroSerde.class);

        props.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 1 * 1000);
        props.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, 0);

        props.put("schema.registry.url", schemaUrl);

        // Custom Serializer if we have avro schema InvoiceAvroSerde
        final Serde<Invoice> InvoiceAvroSerde = new SpecificAvroSerde<>();
        // part of Schema Registry

        // When you want to override serdes explicitly/selectively
        final Map<String, String> serdeConfig = Collections.singletonMap("schema.registry.url",
                schemaUrl);
        // registry schema in the schema registry if not found
        InvoiceAvroSerde.configure(serdeConfig, true); // `true` for record keys

        // In the subsequent lines we define the processing topology of the Streams application.
        final StreamsBuilder builder = new StreamsBuilder();
        // a Stream is a consumer
        final KStream<String, Invoice> invoiceStream = builder.stream("invoices");

        invoiceStream.foreach(new ForeachAction<String, Invoice>() {
            @Override
            public void apply(String key, Invoice invoice) {
                System.out.println("Invoice Key " + key + "  value id  " + invoice.getId() + ":" + invoice.getAmount() );
                System.out.println("received invoice " + invoice);
            }
        });

        // Aggregation, pre-requisties for the aggregation
        KGroupedStream<String, Invoice> stateGroupStream = invoiceStream.groupBy(
                (key, invoice) -> invoice.getState().toString() // return a key (state)
        );

        // KEY, VALUE, table used for aggregation
        // State name, count
        KTable<String, Long> stateGroupCount = stateGroupStream
                .count(); // numebr of orders by state

        final Serde<String> stringSerde = Serdes.String();
        final Serde<Long> longSerde = Serdes.Long();
        final Serde<Double> doubleSerde = Serdes.Double();

        stateGroupCount.toStream().to("statewise-invoices-count", Produced.with(stringSerde, longSerde));

        // collection of streams put together
        final KafkaStreams streams = new KafkaStreams(builder.build(), props);


        try {
            streams.cleanUp();
        }catch(Exception e) {

        }

        streams.start();

        System.out.println("Stream started");

        // Add shutdown hook to respond to SIGTERM and gracefully close Kafka Streams
        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));




    }

}