# Kafka Producer

A Spring Boot application that exposes a REST endpoint and publishes `Student` objects as JSON messages to an Apache Kafka topic.

## Tech Stack

- Java 21
- Spring Boot (Spring Framework 7, Spring Kafka 4.2)
- Apache Kafka
- Jackson 3 (`JacksonJsonSerializer`)

## How It Works

```
Client --POST--> Controller --> MessageService --> KafkaTemplate --> Kafka topic "students"
```

- **Topic:** `students`
- **Message key:** `student`
- **Message value:** `Student` serialized to JSON

## Project Structure

```
src/main/java/com/sp/kafkaproducer
├── controller/Controller.java       # REST endpoint
├── service/MessageService.java      # Sends messages via KafkaTemplate
└── model/Student.java               # Message payload (name, age)
```

## Prerequisites

- JDK 21+
- Maven (or the included wrapper)
- A running Kafka broker on `localhost:9092`

## Configuration

`src/main/resources/application.yml`:

```yaml
spring:
  application:
    name: kafkaproducer
  kafka:
    bootstrap-servers: localhost:9092
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JacksonJsonSerializer

server:
  port: 8081
```

## Run the Application

```bash
./mvnw spring-boot:run
```

The app starts on http://localhost:8081.

## Send a Message

```bash
curl -X POST http://localhost:8081/<your-endpoint> \
  -H "Content-Type: application/json" \
  -d '{"name": "John", "age": 20}'
```

Expected response:

```
Message sent to Kafka students Topic: Student{name='John', age=20}
```

## Verify the Message in Kafka

```bash
kafka-console-consumer.sh --bootstrap-server localhost:9092 \
  --topic students --from-beginning --property print.key=true
```

Output:

```
student	{"name":"John","age":20}
```

## Consumer Notes

A consumer of this topic should use `JacksonJsonDeserializer` (Spring Kafka 4.x) and set the trusted packages:

```yaml
spring:
  kafka:
    consumer:
      value-deserializer: org.springframework.kafka.support.serializer.JacksonJsonDeserializer
      properties:
        spring.json.trusted.packages: com.sp.kafkaproducer.model
```

If the consumer's `Student` class lives in a different package, set `spring.json.value.default.type` to that class instead.

## Troubleshooting

| Error | Cause | Fix |
|-------|-------|-----|
| `Can't convert value of class Student to class StringSerializer` | `value-serializer` not picked up (e.g. `kafka:` nested under `server:` in YAML) | Make sure `kafka:` sits under `spring:` |
| `ClassNotFoundException: com.fasterxml.jackson.core.type.TypeReference` | Old `JsonSerializer` (Jackson 2) used with Jackson 3 | Use `JacksonJsonSerializer` |
