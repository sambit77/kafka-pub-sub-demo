package com.sp.kafkaproducer.service;

import com.sp.kafkaproducer.model.Student;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

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
