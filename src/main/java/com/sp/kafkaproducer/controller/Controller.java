package com.sp.kafkaproducer.controller;

import com.sp.kafkaproducer.model.Student;
import com.sp.kafkaproducer.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/kafka")
public class Controller {

    @Autowired
    private MessageService messageService;

    @GetMapping("/receive")
    public ResponseEntity<String> sendMessage()
    {
        String response = messageService.get();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
