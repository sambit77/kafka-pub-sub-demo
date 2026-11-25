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

## Troubleshooting

| Error | Cause | Fix |
|-------|-------|-----|
| `Can't convert value of class Student to class StringSerializer` | `value-serializer` not picked up (e.g. `kafka:` nested under `server:` in YAML) | Make sure `kafka:` sits under `spring:` |
| `ClassNotFoundException: com.fasterxml.jackson.core.type.TypeReference` | Old `JsonSerializer` (Jackson 2) used with Jackson 3 | Use `JacksonJsonSerializer` |
| `Can't deserialize data from topic [students]` / `Unrecognized token` | Plain-text (non-JSON) records already in the topic | Recreate the topic, or use `ErrorHandlingDeserializer` (see Consumer section) |
| `ClassNotFoundException: com.sp.consumer.model.Student` | `spring.json.value.default.type` points to a class that doesn't exist | Point it to the real `Student` class in your project |

## Consumer

The consumer code lives in the **`kafka-consumer`** branch of this repository.

```bash
git checkout kafka-consumer
```

It reads `Student` JSON messages from the `students` topic and keeps the latest one in memory.

### Consumer Configuration

`src/main/resources/application.yml` (consumer branch):

```yaml
spring:
  application:
    name: consumer
  kafka:
    bootstrap-servers: localhost:9092
    consumer:
      group-id: student-group
      auto-offset-reset: earliest
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.springframework.kafka.support.serializer.ErrorHandlingDeserializer
      properties:
        spring.deserializer.value.delegate.class: org.springframework.kafka.support.serializer.JacksonJsonDeserializer
        spring.json.trusted.packages: com.sp.kafkaproducer.model
        spring.json.value.default.type: com.sp.kafkaproducer.model.Student

server:
  port: 8082
```

- `ErrorHandlingDeserializer` wraps `JacksonJsonDeserializer`, so a malformed record is logged and skipped instead of blocking the consumer in an endless retry loop.
- The consumer runs on port **8082** so it doesn't clash with the producer on 8081.

### Listener

`MessageService` in the consumer branch:

```java
@Service
public class MessageService {

    private String message;

    @KafkaListener(topics = "students", groupId = "student-group")
    public void getMessage(Student student) {
        message = "Message from kafka topic student " + student.toString();
    }

    public String get() {
        return message;
    }
}
```

### Run the Consumer

1. Start Kafka and the producer (see above).
2. Check out the consumer branch and start it:

   ```bash
   git checkout kafka-consumer
   ./mvnw spring-boot:run
   ```

3. POST a student to the producer. The consumer receives it, and `get()` returns the latest message string.

### Consumer Troubleshooting

- **Consumer keeps logging `Can't deserialize data`:** the topic contains non-JSON records from earlier testing. Delete and recreate the `students` topic, or keep `ErrorHandlingDeserializer` so those records are skipped.
- **Port already in use:** make sure the producer (8081) and consumer (8082) use different ports.