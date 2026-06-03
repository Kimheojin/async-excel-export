package com.heojin.async_excel_export.export.service;

import com.heojin.async_excel_export.export.config.JobQueue.ExportJobQueue;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExportJobWorker {

    private final ExportJobQueue exportJobQueue;
    private final ExportJobService exportJobService;
    private final ExcelFileService excelFileService;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @PostConstruct
    void start() {
        exportJobService.recoverJobsOnStartup();
        executorService.submit(this::run);
    }

    @PreDestroy
    void stop() {
        executorService.shutdownNow();
    }

    private void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                Long jobId = exportJobQueue.take();
                process(jobId);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
            } catch (Exception exception) {
                log.error("Unexpected export worker error", exception);
            }
        }
    }

    private void process(Long jobId) {
        try {
            exportJobService.startProcessing(jobId);
            String filePath = excelFileService.generate(jobId);
            exportJobService.complete(jobId, filePath);
        } catch (Exception exception) {
            log.error("Failed to process export job. jobId={}", jobId, exception);
            exportJobService.fail(jobId, exception);
        }
    }
}
