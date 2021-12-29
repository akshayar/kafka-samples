package com.aksh.kafka.fake;

import com.google.gson.Gson;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StringUtils;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.io.FileReader;
import java.util.Optional;
import java.util.Properties;
@Component
public class JSRandomDataGenerator implements IRandomGenerator<Properties> {
    // create a script engine manager
    ScriptEngineManager factory = new ScriptEngineManager();
    // create a Nashorn script engine
    ScriptEngine engine = factory.getEngineByName("nashorn");
    static String FILE_PATH = "src/main/resources/generate-data.js";

    Gson gson=new Gson();

    public Properties evaluateFromJS(String file) {
        try {
            FileReader reader = new FileReader(Optional.ofNullable(file).filter(s -> !StringUtils.isEmpty(s)).orElse(FILE_PATH));
            String script = FileCopyUtils.copyToString(reader);
            engine.eval(script);
            Object javaVar = engine.get("outValue");
            System.out.println(javaVar);
            return (Properties) javaVar;
        } catch (final Exception se) {
            se.printStackTrace();
        }
        return new Properties();
    }

    @Override
    public String createPayload(Class<Properties> type, String templateFile) throws Exception {
        Properties data=evaluateFromJS(templateFile);
        return gson.toJson(data);
    }

    @Override
    public Properties createPayloadObject(Class<Properties> type,String templateFile) throws Exception {
        return  evaluateFromJS(templateFile);
    }
}
