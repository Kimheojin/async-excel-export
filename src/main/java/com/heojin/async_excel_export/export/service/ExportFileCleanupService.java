package com.heojin.async_excel_export.export.service;

import com.heojin.async_excel_export.export.repository.ExportJobRepository;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExportFileCleanupService {

    private final ExportJobRepository exportJobRepository;

    @Scheduled(fixedDelay = 300_000)
    @Transactional
    public void deleteExpiredFiles() {
        exportJobRepository.findAllByExpiresAtBeforeAndFilePathIsNotNull(LocalDateTime.now())
                .forEach(job -> {
                    try {
                        Files.deleteIfExists(Path.of(job.getFilePath()));
                        job.clearFilePath();
                    } catch (IOException exception) {
                        log.warn("Failed to delete expired export file. jobId={}, filePath={}",
                                job.getJobId(), job.getFilePath(), exception);
                    }
                });
    }
}
