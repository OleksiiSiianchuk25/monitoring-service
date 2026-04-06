package com.ajlekc.monitoringservice.service;

import com.ajlekc.monitoringservice.model.User;
import com.ajlekc.monitoringservice.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class UserProcessingService {

    @Autowired
    private UserRepository userRepository;

    public void processAndSave(User user) {
        if (user == null) {
            log.warn("The empty user was found. Saving has been canceled.");
            return;
        }

        userRepository.save(user);
        log.info("User {} (External ID: {}) successfully saved to the database", user.getName(), user.getExternalId());
    }
}
