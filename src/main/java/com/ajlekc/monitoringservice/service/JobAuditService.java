package com.ajlekc.monitoringservice.service;

import com.ajlekc.monitoringservice.model.JobRun;
import com.ajlekc.monitoringservice.repository.JobRunRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;

@Service
@Slf4j
@RequiredArgsConstructor
public class JobAuditService {

    private final JobRunRepository jobRunRepository;
    private final MeterRegistry meterRegistry;

    private final AtomicLong lastSuccessEpochSeconds = new AtomicLong(0L);

    private Counter successCounter;
    private Counter failureCounter;

    @PostConstruct
    public void initMetrics() {
        successCounter = Counter.builder("monitoring.job.runs")
                .description("Number of completed job runs")
                .tag("status", "success")
                .register(meterRegistry);

        failureCounter = Counter.builder("monitoring.job.runs")
                .description("Number of completed job runs")
                .tag("status", "failure")
                .register(meterRegistry);

        Gauge.builder("monitoring.job.last_success_epoch_seconds", lastSuccessEpochSeconds, AtomicLong::get)
                .description("Epoch seconds of the last successful job run")
                .register(meterRegistry);
    }

    public JobRun start() {
        JobRun run = JobRun.builder()
                .startedAt(Instant.now())
                .status(JobRun.JobStatus.RUNNING)
                .build();
        return jobRunRepository.save(run);
    }

    public void finishSuccess(JobRun run, JobRunCounters counters) {
        run.setFinishedAt(Instant.now());
        run.setStatus(JobRun.JobStatus.SUCCESS);
        run.setFetchedCount(counters.fetched());
        run.setNewCount(counters.created());
        run.setUpdatedCount(counters.updated());
        run.setUnchangedCount(counters.unchanged());
        run.setSkippedCount(counters.skipped());
        jobRunRepository.save(run);

        successCounter.increment();
        lastSuccessEpochSeconds.set(run.getFinishedAt().getEpochSecond());
        log.info("Job run {} finished SUCCESS: {}", run.getId(), counters);
    }

    public void finishFailure(JobRun run, String errorMessage) {
        run.setFinishedAt(Instant.now());
        run.setStatus(JobRun.JobStatus.FAILURE);
        run.setErrorMessage(errorMessage);
        jobRunRepository.save(run);

        failureCounter.increment();
        log.warn("Job run {} finished FAILURE: {}", run.getId(), errorMessage);
    }

    public static class JobRunCounters {
        private int fetched;
        private int created;
        private int updated;
        private int unchanged;
        private int skipped;

        public void incFetched()   { fetched++; }
        public void incCreated()   { created++; }
        public void incUpdated()   { updated++; }
        public void incUnchanged() { unchanged++; }
        public void incSkipped()   { skipped++; }

        public int fetched()   { return fetched; }
        public int created()   { return created; }
        public int updated()   { return updated; }
        public int unchanged() { return unchanged; }
        public int skipped()   { return skipped; }

        @Override
        public String toString() {
            return "fetched=" + fetched + ", new=" + created + ", updated=" + updated
                    + ", unchanged=" + unchanged + ", skipped=" + skipped;
        }
    }
}