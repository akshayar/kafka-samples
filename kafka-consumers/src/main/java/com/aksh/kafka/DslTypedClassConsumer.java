package com.aksh.kafka;


import com.aksh.kafka.avro.fake.TradeData;
import com.mitchseymour.kafka.serialization.avro.AvroSerdes;
import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;
import io.confluent.kafka.streams.serdes.avro.GenericAvroSerde;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Printed;

import java.util.Collections;
import java.util.Properties;

public class DslTypedClassConsumer {
    //    @Value("${topicName:}")
    private static String TOPIC_NAME_GENERIC = "data-stream-ingest";
    private static String TOPIC_NAME_TRADE="data-stream-ingest-trade";

    public static void main(String[] args) {
        Topology topology = getTopologyForTradeInfo();


        Properties config = new Properties();
        config.put(StreamsConfig.APPLICATION_ID_CONFIG, "ConsumerApp");
        config.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:29092");

        KafkaStreams streams = new KafkaStreams(topology, config);
        streams.start();

        System.out.println("Starting  streams");
        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));


    }


    private static Topology getTopologyForGenericRecord() {
        final String topicName=TOPIC_NAME_GENERIC;
        StreamsBuilder builder = new StreamsBuilder();
        Serde<GenericRecord> genericAvroSerde = new GenericAvroSerde();
        boolean isKeySerde = false;
        genericAvroSerde.configure(
                Collections.singletonMap(
                        AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG,
                        "http://localhost:8081/"),
                isKeySerde);
        KStream<String, GenericRecord> stream =
                builder.<String, GenericRecord>stream(topicName, Consumed.with(Serdes.String(), genericAvroSerde));


        stream.print(Printed.<String, GenericRecord>toSysOut().withLabel(topicName));
        return builder.build();
    }

    private static Topology getTopologyForTradeInfo() {
        final String topicName=TOPIC_NAME_TRADE;
        StreamsBuilder builder = new StreamsBuilder();

        Serde<TradeData> genericAvroSerde = AvroSerdes.get(TradeData.class);
        boolean isKeySerde = false;
        genericAvroSerde.configure(
                Collections.singletonMap(
                        AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG,
                        "http://localhost:8081/"),
                isKeySerde);
        KStream<String, TradeData> stream =
                builder.<String, TradeData>stream(topicName, Consumed.with(Serdes.String(), genericAvroSerde));


        stream.print(Printed.<String, TradeData>toSysOut().withLabel(topicName));
        return builder.build();
    }


}
