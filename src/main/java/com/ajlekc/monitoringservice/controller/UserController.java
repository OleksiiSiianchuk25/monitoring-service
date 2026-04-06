package com.ajlekc.monitoringservice.controller;

import com.ajlekc.monitoringservice.model.User;
import com.ajlekc.monitoringservice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {
    @Autowired
    private UserRepository userRepository;

    @PostMapping("/addUser")
    public void addUser(@RequestBody User user) {
        userRepository.save(user);
    }

}
