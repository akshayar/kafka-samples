package com.aksh.kafka.sink;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.util.StringInputStream;
import org.apache.kafka.connect.errors.ConnectException;
import org.apache.kafka.connect.sink.SinkRecord;
import org.apache.kafka.connect.sink.SinkTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collection;
import java.util.Map;


public class S3SinkTask extends SinkTask {
    private static final Logger log = LoggerFactory.getLogger(S3SinkTask.class);

    private String bucket;
    private String prefix;
    private String region;

    private AmazonS3 s3 ;


    @Override
    public String version() {
        return new S3SinkConnector().version();
    }

    @Override
    public void start(Map<String, String> props) {
        bucket = props.get(S3SinkConnector.S3_BUCKET_CONFIG);
        prefix=props.get(S3SinkConnector.S3_PATH_CONFIG);
        region=props.get(S3SinkConnector.REGION_CONFIG);

        log.info("Starting, bucket:"+bucket+",prefix:"+prefix);

        s3=AmazonS3ClientBuilder.standard().withRegion(region).build();
        try {
            String filename=System.currentTimeMillis()+"started.txt";
            new File(filename).createNewFile();
            s3.putObject(bucket,prefix+"/"+filename,new File(filename));
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public void put(Collection<SinkRecord> sinkRecords) {
        String filename="fileout"+"-"+System.currentTimeMillis()+".txt";
        log.info("Put "+filename+"bucket:"+bucket+",prefix:"+prefix);
        try {
            PrintStream outputStream = new PrintStream(
                    Files.newOutputStream(Paths.get(filename), StandardOpenOption.CREATE, StandardOpenOption.APPEND),
                    false,
                    StandardCharsets.UTF_8.name());

            for (SinkRecord record : sinkRecords) {
                log.trace("Writing line to {}: {}", logFilename(), record.value());
                outputStream.println(record.value());
            }
            outputStream.flush();
            s3.putObject(bucket,prefix+"/"+filename,new File(filename));
        } catch (IOException e) {
            e.printStackTrace();
            throw new ConnectException("Couldn't find or create file '" + filename + "' for FileStreamSinkTask", e);
        }


    }

    @Override
    public void stop() {

    }

    private String logFilename() {
        return bucket == null ? "stdout" : bucket;
    }
}
