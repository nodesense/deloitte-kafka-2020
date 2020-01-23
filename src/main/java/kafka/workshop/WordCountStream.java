// WordCount.java
package kafka.workshop;

import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.*;
import org.apache.kafka.streams.kstream.ForeachAction;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Produced;

import java.time.Duration;
import java.util.*;

// kafka-topics --zookeeper localhost:2181 --create --topic words --replication-factor 3 --partitions 3
//// kafka-topics --zookeeper localhost:2181 --create --topic words-count-output --replication-factor 3 --partitions 3



// kafka-console-consumer --bootstrap-server k5.nodesense.ai:9092 --topic words-count-output --from-beginning --property print.key=true  --property value.deserializer=org.apache.kafka.common.serialization.LongDeserializer


   //     kafka-console-consumer --bootstrap-server k5.nodesense.ai:9092 --topic words-count-windowed-output --from-beginning --property print.key=true  --property value.deserializer=org.apache.kafka.common.serialization.LongDeserializer


import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.*;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class WordCountStream {

    public static Properties getConfiguration() {
        final String bootstrapServers = "k5.nodesense.ai:9092";
        String schemaUrl = "http://k5.nodesense.ai:8081";

        final Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "word-count2-stream");
        props.put(StreamsConfig.CLIENT_ID_CONFIG, "word-count2-stream-client");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());


        props.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 1 * 1000);
        props.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, 0);


        props.put("schema.registry.url", schemaUrl);
        return props;
    }


    public static void main(final String[] args) throws Exception {
        System.out.println("Running WordCount Stream");

        Properties props = getConfiguration();

        final Serde<String> stringSerde = Serdes.String();
        final Serde<Long> longSerde = Serdes.Long();


        // In the subsequent lines we define the processing topology of the Streams application.
        final StreamsBuilder builder = new StreamsBuilder();

        // Consumer
        final KStream<String, String> lines = builder
                .stream("words");


        lines.foreach(new ForeachAction<String, String>() {
            @Override
            public void apply(String key, String value) {
                System.out.println("Full Line " + key + " Value is  *" + value + "*" );
            }
        });


        // STRAEM PROCESSING
        final KStream<String, String>  nonEmptyLines = lines.filter( (key, value) -> !value.isEmpty());



        KStream<String, String> splitWords = nonEmptyLines
                .flatMapValues(line -> Arrays.asList(line.toLowerCase().split("\\W+")));

        splitWords.foreach(new ForeachAction<String, String>() {
            @Override
            public void apply(String key, String value) {
                System.out.println("Split Word " + key + " Value is  *" + value + "*" );
            }
        });


        // splitwords has individual words as input, "apple", "orange", "apple"

        KTable<String, Long> wordCount = splitWords
                .groupBy((_$, word) -> word)
                .count();


        KStream<String, Long> wordCountStream = wordCount.toStream();



          splitWords
                .groupBy((_$, word) -> word)
                .windowedBy(TimeWindows.of(Duration.ofSeconds(120)))
                .count()
                .toStream()
                .foreach((windowedWord, count) -> {
                        System.out.println("Starting " + windowedWord.window().start());
                        System.out.println("End " + windowedWord.window().end());
                        System.out.println("Windows word is " + windowedWord.key() + " Count is " + count);
                });


        //  .windowedBy( SessionWindows.with(TimeUnit.MINUTES.toMillis(5)))) /* session window */
        // TimeWindows.of()  tumbling windows

        /* Hopping Window
        long windowSizeMs = TimeUnit.MINUTES.toMillis(5); // 5 * 60 * 1000L

        long advanceMs =    TimeUnit.MINUTES.toMillis(1); // 1 * 60 * 1000L
        .windowedBy( TimeWindows.of(windowSizeMs).advanceBy(advanceMs))
        */

        splitWords
                .groupBy((_$, word) -> word)
                .windowedBy(TimeWindows.of(Duration.ofSeconds(120)))
                .count()
                .toStream()
                // Convert Windows<String> to <Stirng, Value>
                .map( (windowedKey, value) -> new KeyValue<>(windowedKey.key(), value) )
                .foreach((word, count) -> {
                    System.out.println("Windows word is " + word + " Count is " + count);
                });

        KStream<String, Long> windowed = splitWords
                .groupBy((_$, word) -> word)
                 .windowedBy(TimeWindows.of(Duration.ofSeconds(120)))
                .count()
                .toStream()
                .map( (key, value) -> new KeyValue<>(key.key(), value) );


        windowed.to("words-count-windowed-output", Produced.with(stringSerde, longSerde));



        wordCountStream.foreach(new ForeachAction<String, Long>() {
            @Override
            public void apply(String word, Long count) {
                System.out.println("Word " + word + " Count is  *" + count + "*" );
            }
        });


        // STREAM PROCESSSING

        // Producer
        wordCountStream.to("words-count-output", Produced.with(stringSerde, longSerde));




        final KafkaStreams streams = new KafkaStreams(builder.build(), props);

        try {
            streams.cleanUp();
        }catch(Exception e) {
            System.out.println("Error While cleaning state" + e);
        }
        streams.start();

        // Add shutdown hook to respond to SIGTERM and gracefully close Kafka Streams
        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
    }

}