package com.ajlekc.monitoringservice.integration;

import com.ajlekc.monitoringservice.model.JobRun;
import com.ajlekc.monitoringservice.repository.JobRunRepository;
import com.ajlekc.monitoringservice.service.JobAuditService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "app.scheduling.enabled=false",
        "external.mock-api.base-url=http://localhost:9999/mock-users"
})
@Testcontainers
class JobAuditIntegrationTest extends TestcontainersConfig {

    @Autowired
    private JobAuditService jobAuditService;

    @Autowired
    private JobRunRepository jobRunRepository;

    @BeforeEach
    void setUp() {
        jobRunRepository.deleteAll();
    }

    @Test
    void shouldPersistJobRunOnStart() {
        JobRun run = jobAuditService.start();

        assertThat(run.getId()).isNotNull();
        assertThat(run.getStatus()).isEqualTo(JobRun.JobStatus.RUNNING);
        assertThat(run.getStartedAt()).isNotNull();
        assertThat(run.getFinishedAt()).isNull();
    }

    @Test
    void shouldMarkJobAsSuccessWithCounters() {
        JobRun run = jobAuditService.start();

        JobAuditService.JobRunCounters counters = new JobAuditService.JobRunCounters();
        counters.incFetched();
        counters.incCreated();

        jobAuditService.finishSuccess(run, counters);

        JobRun saved = jobRunRepository.findById(run.getId()).orElseThrow();
        assertThat(saved.getStatus()).isEqualTo(JobRun.JobStatus.SUCCESS);
        assertThat(saved.getFinishedAt()).isNotNull();
        assertThat(saved.getFetchedCount()).isEqualTo(1);
        assertThat(saved.getNewCount()).isEqualTo(1);
        assertThat(saved.getUpdatedCount()).isEqualTo(0);
        assertThat(saved.getErrorMessage()).isNull();
    }

    @Test
    void shouldMarkJobAsFailureWithErrorMessage() {
        JobRun run = jobAuditService.start();

        jobAuditService.finishFailure(run, "Connection refused");

        JobRun saved = jobRunRepository.findById(run.getId()).orElseThrow();
        assertThat(saved.getStatus()).isEqualTo(JobRun.JobStatus.FAILURE);
        assertThat(saved.getFinishedAt()).isNotNull();
        assertThat(saved.getErrorMessage()).isEqualTo("Connection refused");
    }

    @Test
    void shouldReturnJobRunsOrderedByMostRecent() throws InterruptedException {
        JobRun run1 = jobAuditService.start();
        jobAuditService.finishSuccess(run1, new JobAuditService.JobRunCounters());

        Thread.sleep(10); // ensure different timestamps

        JobRun run2 = jobAuditService.start();
        jobAuditService.finishFailure(run2, "error");

        Page<JobRun> runs = jobRunRepository.findAllByOrderByStartedAtDesc(PageRequest.of(0, 10));

        assertThat(runs.getContent()).hasSize(2);
        assertThat(runs.getContent().get(0).getId()).isEqualTo(run2.getId());
        assertThat(runs.getContent().get(1).getId()).isEqualTo(run1.getId());
    }
}
