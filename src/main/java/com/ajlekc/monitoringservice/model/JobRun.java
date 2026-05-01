package com.ajlekc.monitoringservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "job_runs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobRun {

    @Id
    private String id;

    private Instant startedAt;
    private Instant finishedAt;

    private JobStatus status;

    private int fetchedCount;
    private int newCount;
    private int updatedCount;
    private int unchangedCount;
    private int skippedCount;

    private String errorMessage;

    public enum JobStatus {
        RUNNING, SUCCESS, FAILURE
    }
}