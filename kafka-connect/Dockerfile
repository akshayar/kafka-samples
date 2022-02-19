FROM debezium/connect-base:1.8

LABEL maintainer="Debezium Community"

ENV DEBEZIUM_VERSION="1.8.0.Final" \
    MAVEN_REPO_CENTRAL="" \
    MAVEN_REPOS_ADDITIONAL="" \
    MAVEN_DEP_DESTINATION=$KAFKA_CONNECT_PLUGINS_DIR \
    MONGODB_MD5=9b4d6d99ef5b9d9405de44773ba54463 \
    MYSQL_MD5=9485b648d4f237791478a62ccc41eb37 \
    POSTGRES_MD5=93e991649f7681d75d9f6d8c6d66f8d8 \
    SCRIPTING_MD5=342dc6b2fb48c8200162e340ddc582d5

ENV AWS_MSK_IAM_AUTH=""
ENV CONNECT_S3="10.0.4"

RUN docker-maven-download debezium mongodb "$DEBEZIUM_VERSION" "$MONGODB_MD5" && \
    docker-maven-download debezium mysql "$DEBEZIUM_VERSION" "$MYSQL_MD5" && \
    docker-maven-download debezium postgres "$DEBEZIUM_VERSION" "$POSTGRES_MD5" && \
    docker-maven-download debezium-optional scripting "$DEBEZIUM_VERSION" "$SCRIPTING_MD5"

RUN curl -sfSL -o "$EXTERNAL_LIBS_DIR/aws-msk-iam-auth-1.1.1-all.jar" "https://github.com/aws/aws-msk-iam-auth/releases/download/v1.1.1/aws-msk-iam-auth-1.1.1-all.jar"

COPY target/kafka-connect-sample.jar $KAFKA_HOME/connect/


