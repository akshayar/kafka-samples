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

public class FileStreamSinkConnector extends SinkConnector {
    public static final String FILE_CONFIG = "file";
    private static final ConfigDef CONFIG_DEF = new ConfigDef()
            .define(FILE_CONFIG, ConfigDef.Type.STRING, null, ConfigDef.Importance.HIGH, "Destination filename. If not specified, the standard output will be used");

    private AbstractConfig parsedConfig;
    private String filename;

    @Override
    public void start(Map<String, String> map) {
        parsedConfig = new AbstractConfig(CONFIG_DEF, map);
        filename = parsedConfig.getString(FILE_CONFIG);
    }

    @Override
    public void stop() {

    }

    @Override
    public Class<? extends Task> taskClass() {
        return FileStreamSinkTask.class;
    }

    @Override
    public List<Map<String, String>> taskConfigs(int maxTasks) {
        ArrayList<Map<String, String>> configs = new ArrayList<>();
        for (int i = 0; i < maxTasks; i++) {
            Map<String, String> config = new HashMap<>();
            if (filename != null)
                config.put(FILE_CONFIG, filename);
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
