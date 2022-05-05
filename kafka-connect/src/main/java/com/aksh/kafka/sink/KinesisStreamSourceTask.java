package com.aksh.kafka.sink;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.kinesis.AmazonKinesis;
import com.amazonaws.services.kinesis.AmazonKinesisAsyncClient;
import com.amazonaws.services.kinesis.model.*;
import com.amazonaws.util.Base64;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.errors.ConnectException;
import org.apache.kafka.connect.source.SourceRecord;
import org.apache.kafka.connect.source.SourceTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class KinesisStreamSourceTask extends SourceTask {
    public static final String KINESIS_STREAM = "streamName";
    public static final String POSITION_FIELD = "position";
    private static final Logger log = LoggerFactory.getLogger(KinesisStreamSourceTask.class);
    private static final Schema VALUE_SCHEMA = Schema.STRING_SCHEMA;

    private String streamName;
    private String region;
    AmazonKinesis client ;
    private char[] buffer;
    private int offset = 0;
    private String topic = null;
    private int batchSize = FileStreamSourceConnector.DEFAULT_TASK_BATCH_SIZE;

    private Long streamOffset;

    public KinesisStreamSourceTask() {
        this(1024);
    }

    /* visible for testing */
    KinesisStreamSourceTask(int initialBufferSize) {
        buffer = new char[initialBufferSize];
    }

    @Override
    public String version() {
        return new FileStreamSourceConnector().version();
    }

    @Override
    public void start(Map<String, String> props) {
        region=props.get(S3SinkConnector.REGION_CONFIG);
        streamName = props.get(KinesisSourceConnector.KINESIS_CONFIG);
        if (streamName == null || streamName.isEmpty()) {
            throw new RuntimeException("Null Kineis Stream");
        }
        client= AmazonKinesisAsyncClient.builder()
                .withRegion(region)
                .build();
        DescribeStreamRequest describeStreamRequest = new DescribeStreamRequest()
                .withStreamName(streamName);
        List<Shard> shards = new ArrayList<>();
        DescribeStreamResult streamRes;
        do {
            streamRes = client.describeStream(describeStreamRequest);
            shards.addAll(streamRes.getStreamDescription().getShards());

        } while (streamRes.getStreamDescription().getHasMoreShards());
        // Missing topic or parsing error is not possible because we've parsed the config in the
        // Connector
        topic = props.get(FileStreamSourceConnector.TOPIC_CONFIG);
        batchSize = Integer.parseInt(props.get(FileStreamSourceConnector.TASK_BATCH_SIZE_CONFIG));
        log.info("Starting streamName:"+streamName+",topic:"+topic+",batchSize:"+batchSize);
    }

    @Override
    public List<SourceRecord> poll() throws InterruptedException {
        log.info("Polling:streamName"+streamName);
        DescribeStreamRequest describeStreamRequest = new DescribeStreamRequest()
                .withStreamName(streamName);
        List<Shard> shards = new ArrayList<>();
        DescribeStreamResult streamRes;
        do {
            streamRes = client.describeStream(describeStreamRequest);
            shards.addAll(streamRes.getStreamDescription().getShards());

        } while (streamRes.getStreamDescription().getHasMoreShards());

        return shards.stream().flatMap((Shard s)->{
            GetShardIteratorRequest itReq = new GetShardIteratorRequest()
                    .withStreamName(streamName)
                    .withShardIteratorType("LATEST")
                    .withShardId(s.getShardId());
            GetShardIteratorResult shardIteratorResult = client.getShardIterator(itReq);
            String shardIterator=shardIteratorResult.getShardIterator();
            GetRecordsRequest recordsRequest = new GetRecordsRequest()
                    .withShardIterator(shardIterator)
                    .withLimit(100);
            GetRecordsResult result = client.getRecords(recordsRequest);
            return Optional.ofNullable(result.getRecords()).orElse(Collections.emptyList()).stream();
        }).map(r->{
            try {

                return new SourceRecord(offsetKey(r.getPartitionKey()), offsetValue(r.getSequenceNumber()), topic, null,
                        null, null, null, new String(r.getData().array()), System.currentTimeMillis());
            } catch (RuntimeException e) {
                e.printStackTrace();
            }
            return null;
        }).filter(Objects::nonNull).collect(Collectors.toList());

    }


    @Override
    public void stop() {
        log.trace("Stopping");
        synchronized (this) {
            try {
                client.shutdown();
            } catch (RuntimeException e) {
                log.error("Failed to close FileStreamSourceTask stream: ", e);
            }
            this.notify();
        }
    }

    private Map<String, String> offsetKey(String filename) {
        return Collections.singletonMap(KINESIS_STREAM, filename);
    }

    private Map<String, String> offsetValue(String pos) {
        return Collections.singletonMap(POSITION_FIELD, pos);
    }

    private String logFilename() {
        return streamName == null ? "stdin" : streamName;
    }

    /* visible for testing */
    int bufferSize() {
        return buffer.length;
    }
}
