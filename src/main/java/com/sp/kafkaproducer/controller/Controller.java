package com.sp.kafkaproducer.controller;

import com.sp.kafkaproducer.model.Student;
import com.sp.kafkaproducer.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/kafka")
public class Controller {

    @Autowired
    private MessageService messageService;

    @PostMapping("/send")
    public ResponseEntity<String> sendMessage(@RequestBody Student student)
    {
        String response = messageService.sendMessage(student);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
