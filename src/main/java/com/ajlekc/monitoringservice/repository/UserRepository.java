package com.ajlekc.monitoringservice.repository;

import com.ajlekc.monitoringservice.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String> {
//    Optional<User> findByExternalId(Integer externalId);

        Optional<User> findFirstByExternalIdOrderByInternalId(Integer externalId);
}
