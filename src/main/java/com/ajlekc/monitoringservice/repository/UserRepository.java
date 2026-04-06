package com.ajlekc.monitoringservice.repository;

import com.ajlekc.monitoringservice.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserRepository extends MongoRepository<User, Integer> {
}
