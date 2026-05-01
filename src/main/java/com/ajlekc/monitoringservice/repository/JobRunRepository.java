package com.ajlekc.monitoringservice.repository;

import com.ajlekc.monitoringservice.model.JobRun;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface JobRunRepository extends MongoRepository<JobRun, String> {

    Page<JobRun> findAllByOrderByStartedAtDesc(Pageable pageable);
}
