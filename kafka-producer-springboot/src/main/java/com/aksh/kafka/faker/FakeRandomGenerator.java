package com.aksh.kafka.faker;

import com.google.gson.Gson;
import org.apache.commons.beanutils.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.lang.reflect.InvocationTargetException;
import java.util.Properties;

@Component
public class FakeRandomGenerator implements IRandomGenerator<Object> {
    @Autowired
    JSRandomDataGenerator jsRandomDataGenerator;
    private Gson gson=new Gson();
    Class clazz;
    @PostConstruct
    void init() throws Exception{
    }
    @Override
    public  String createPayload(Class type,String templateFile) throws Exception {
        return gson.toJson(createPayloadObject(type,templateFile) );
    }

    @Override
    public Object createPayloadObject(Class type,String templateFile) throws Exception {
        Object target= type.newInstance();
        Properties data=jsRandomDataGenerator.createPayloadObject(null,templateFile);
        copyData(data,target);
        return target;
    }


    private void copyData(Properties data,Object target){
        data.entrySet().stream().forEach(entry->{
            try {
                BeanUtils.setProperty(target,entry.getKey()+"",entry.getValue());
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        });
    }
}
