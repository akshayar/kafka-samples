## Goal
The goal of this project is to demonstrate writing Kafka Connect source and sink. 
For the source code refer https://github.com/apache/kafka/tree/3.0/connect/file

## Build 
1. Build the project. 
```shell
mvn clean package -DskipTests
```

## Sample Deployment Using Docker Compose
1. Bring Up Kafka and Kafka Connect with sample connect. 
```shell
export DEBEZIUM_VERSION=1.8
docker-compose -f docker-compose-rdsmysql.yaml up --build
```
2. Register MySQL Source Connector to push CDC data to a topic. 
```json
curl -X DELETE http://localhost:8083/connectors/mysql-inventory-connector
curl -i -X POST -H "Accept:application/json" -H  "Content-Type:application/json" \
    http://localhost:8083/connectors/ \
    -d '{
          "name": "mysql-inventory-connector",
          "config": {
            "connector.class": "io.debezium.connector.mysql.MySqlConnector",
            "tasks.max": "1",
            "database.hostname": "mysql",
            "database.port": "3306",
            "database.user": "debezium",
            "database.password": "dbz",
            "database.server.id": "184054",
            "database.server.name": "dbserver1",
            "database.include.list": "inventory",
            "database.history.kafka.bootstrap.servers": "kafka:9092",
            "database.history.kafka.topic": "schema-changes.inventory"
          }
      }'
curl -X GET http://localhost:8083/connectors/mysql-inventory-connector/status 
``` 
3. Verify that topics are created by debezium CDC process and CDC data is ingested. 
```shell
docker-compose -f docker-compose-rdsmysql.yaml exec kafka /kafka/bin/kafka-topics.sh \
   --bootstrap-server kafka:9092 \
   --list   
docker-compose -f docker-compose-rdsmysql.yaml exec kafka /kafka/bin/kafka-console-consumer.sh \
   --bootstrap-server kafka:9092 \
   --from-beginning \
   --property print.key=true \
   --topic dbserver1.inventory.customers
   
``` 
4. Register File Sink and sink the data pushed through CDC -> topics to a sink file. 
```json
curl -X DELETE http://localhost:8083/connectors/kafka-connect-sample-file-sink

curl -i -X POST -H "Accept:application/json" -H  "Content-Type:application/json" \
 http://localhost:8083/connectors/ \
 -d '{
  "name": "kafka-connect-sample-file-sink",
  "config": {
    "connector.class": "com.aksh.kafka.sink.FileStreamSinkConnector",
    "topics.regex": "dbserver1.inventory.customers",
    "tasks.max": "1",
    "file": "dbserver1-inventory-customers-out.txt"
  }
}'

curl -X GET http://localhost:8083/connectors/kafka-connect-sample-file-sink/status
```
5. Verify the data is being pushed to the file specified in the sink configuration. 
```shell
docker-compose -f docker-compose-rdsmysql.yaml exec connect cat /kafka/dbserver1-inventory-customers-out.txt
```
6. Register File Source connector which reads from the sink file created above. 
```json
curl -X DELETE http://localhost:8083/connectors/kafka-connect-sample-file-source
curl -i -X POST -H "Accept:application/json" -H  "Content-Type:application/json" \
 http://localhost:8083/connectors/ \
 -d '{
  "name": "kafka-connect-sample-file-source",
  "config": {
    "connector.class": "com.aksh.kafka.sink.FileStreamSourceConnector",
    "tasks.max": "1",
    "file": "dbserver1-inventory-customers-out.txt",
    "topic": "filesource.inventory.customers"
  }
}'
curl -X GET http://localhost:8083/connectors/kafka-connect-sample-file-source/status
```

7. Verify that the source connector is reading from the file and pushing data to topics. 
```shell
docker-compose -f docker-compose-rdsmysql.yaml exec kafka /kafka/bin/kafka-console-consumer.sh \
   --bootstrap-server kafka:9092 \
   --from-beginning \
   --property print.key=true \
   --topic filesource.inventory.customers
```

8. Add more data to inventory.customers. 
```shell
docker-compose -f docker-compose-rdsmysql.yaml exec mysql bash -c 'mysql -u $MYSQL_USER -p$MYSQL_PASSWORD inventory'

insert into customers (id,first_name, last_name, email) values(1009,'Akshay','Rawat','akshaya.testing@gmail.com') ;
   
```
9. Verify that new data is coming the final topic. 
```shell

docker-compose -f docker-compose-rdsmysql.yaml exec kafka /kafka/bin/kafka-console-consumer.sh \
   --bootstrap-server kafka:9092 \
   --from-beginning \
   --property print.key=true \
   --topic filesource.inventory.customers
```