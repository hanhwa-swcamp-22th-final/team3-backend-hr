package com.ohgiraffers.team3backendhr.config;

import com.ohgiraffers.team3backendhr.hr.command.application.service.EvaluationPeriodCommandService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EvaluationPeriodSnapshotBackfillRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(EvaluationPeriodSnapshotBackfillRunner.class);

    private final EvaluationPeriodCommandService evaluationPeriodCommandService;

    @Value("${hr.backfill.evaluation-period-snapshots-on-startup:false}")
    private boolean enabled;

    @Override
    public void run(ApplicationArguments args) {
        if (!enabled) {
            return;
        }

        int count = evaluationPeriodCommandService.republishSnapshots();
        log.info("Completed evaluation period snapshot backfill on startup. count={}", count);
    }
}