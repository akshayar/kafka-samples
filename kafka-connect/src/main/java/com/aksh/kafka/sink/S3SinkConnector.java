package com.aksh.kafka.sink;

import org.apache.kafka.common.config.AbstractConfig;
import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.common.utils.AppInfoParser;
import org.apache.kafka.connect.connector.Task;
import org.apache.kafka.connect.sink.SinkConnector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class S3SinkConnector extends SinkConnector {
    public static final String S3_BUCKET_CONFIG = "bucket";
    public static final String S3_PATH_CONFIG = "prefix";
    public static final String REGION_CONFIG = "region";
    private static final ConfigDef CONFIG_DEF = new ConfigDef()
            .define(S3_BUCKET_CONFIG, ConfigDef.Type.STRING, null, ConfigDef.Importance.HIGH, "Destination Bucket")
            .define(S3_PATH_CONFIG, ConfigDef.Type.STRING, null, ConfigDef.Importance.HIGH, "Destination prefix")
            .define(REGION_CONFIG, ConfigDef.Type.STRING, null, ConfigDef.Importance.HIGH, "Region");

    private AbstractConfig parsedConfig;
    private String bucket;
    private String prefix;
    private String region;

    @Override
    public void start(Map<String, String> map) {
        parsedConfig = new AbstractConfig(CONFIG_DEF, map);
        bucket = parsedConfig.getString(S3_BUCKET_CONFIG);
        prefix = parsedConfig.getString(S3_PATH_CONFIG);
        region=parsedConfig.getString(REGION_CONFIG);

    }

    @Override
    public void stop() {

    }

    @Override
    public Class<? extends Task> taskClass() {
        return S3SinkTask.class;
    }

    @Override
    public List<Map<String, String>> taskConfigs(int maxTasks) {
        ArrayList<Map<String, String>> configs = new ArrayList<>();
        for (int i = 0; i < maxTasks; i++) {
            Map<String, String> config = new HashMap<>();
            if (bucket != null)
                config.put(S3_BUCKET_CONFIG, bucket);
            if (prefix != null)
                config.put(S3_PATH_CONFIG, prefix);
            if (region != null)
                config.put(REGION_CONFIG, region);
            configs.add(config);
        }
        return configs;
    }


    @Override
    public ConfigDef config() {
        return CONFIG_DEF;
    }

    @Override
    public String version() {
        return AppInfoParser.getVersion();
    }
}
