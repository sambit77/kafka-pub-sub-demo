package com.sp.kafkaproducer.service;

import com.sp.kafkaproducer.model.Student;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class MessageService {

    @Autowired
    private KafkaTemplate<String, Student> kafkaTemplate;

    public String sendMessage(Student student)
    {
        kafkaTemplate.send("students", "student" ,student);
        return "Message sent to Kafka students Topic: " + student.toString();
    }
}
