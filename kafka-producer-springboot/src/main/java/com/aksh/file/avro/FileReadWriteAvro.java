package com.aksh.file.avro;

import org.apache.avro.Schema;
import org.apache.avro.file.CodecFactory;
import org.apache.avro.file.DataFileReader;
import org.apache.avro.file.DataFileWriter;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.DatumWriter;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class FileReadWriteAvro {
    //"src/main/avro/com/aksh/kafka/avro/fake/TradeData.avsc"
    private Schema readSchema(String schemaPath) throws IOException {
        return  new Schema.Parser().parse(new File(schemaPath));
    }


    public void writeDataPropertiesObject(String fileSchema, String dataPath, Collection<Properties> dataInList) throws IOException{
        final Schema schema=readSchema(fileSchema);

        List<GenericRecord> dataToWrite=Optional.ofNullable(dataInList).orElse(Collections.emptyList()).stream().filter(Objects::nonNull).map(data->{
            GenericRecord avroRecord=new GenericData.Record(schema);
            data.entrySet().stream().forEach(entry->{
                avroRecord.put(entry.getKey()+"",entry.getValue());
            });
            return avroRecord;
        }).collect(Collectors.toList());


        writeDataGenericRecords(schema,dataPath, dataToWrite);
    }

    public void writeDataGenericRecords(String schemeFile, String dataPath, List<GenericRecord> dataToWrite) throws IOException{
        final Schema schema=readSchema(schemeFile);
        writeDataGenericRecords(schema,dataPath,dataToWrite);

    }
    public void writeDataGenericRecords(Schema schema, String dataPath, List<GenericRecord> dataToWrite) throws IOException {
        final DatumWriter<GenericRecord> datumWriter = new GenericDatumWriter<>(schema);
        final FileOutputStream out = new FileOutputStream(dataPath);
        final DataFileWriter<GenericRecord> dataFileWriter = new DataFileWriter<>(datumWriter);
        dataFileWriter.setCodec(CodecFactory.deflateCodec(1));
        dataFileWriter.create(schema, out);
        dataToWrite.forEach(avroRecord->{
            try {
                dataFileWriter.append(avroRecord);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        dataFileWriter.close();
    }

    public List<Properties> readDattaAsPropertiesObject(String fileSchema, String filePath) throws IOException{
        final Schema schema=readSchema(fileSchema);
        List<GenericRecord> dataList = readDataAsGenericRecords(schema,filePath);
        System.out.println(dataList);
        return dataList.stream().map(data->{
            Properties prop=new Properties();
            data.getSchema().getFields().stream().forEach(field -> {
                prop.put(field.name(),data.get(field.name()));
            });
            return prop;
        }).collect(Collectors.toList());

    }

    public List<GenericRecord> readDataAsGenericRecords(String schemaPath, String filePath) throws IOException{
        final Schema schema=readSchema(schemaPath);
        return readDataAsGenericRecords(schema,filePath);
    }


    public List<GenericRecord> readDataAsGenericRecords(Schema schema, String filePath) throws IOException {
        final DatumReader<GenericRecord> dataDatumReader = new GenericDatumReader<>(schema);
        DataFileReader<GenericRecord> dataFileReader = new DataFileReader<GenericRecord>(new File(filePath),dataDatumReader);
        List<GenericRecord> dataList = new ArrayList<>();
        while (dataFileReader.hasNext()) {
            dataList.add( dataFileReader.next());
        }
        return dataList;
    }


}
