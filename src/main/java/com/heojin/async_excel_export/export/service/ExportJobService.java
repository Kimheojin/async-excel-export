package com.heojin.async_excel_export.export.service;

import com.heojin.async_excel_export.export.config.ExportProperties;
import com.heojin.async_excel_export.export.config.JobQueue.ExportJobQueue;
import com.heojin.async_excel_export.export.domain.ExportJob;
import com.heojin.async_excel_export.export.domain.ExportJobStatus;
import com.heojin.async_excel_export.export.repository.ExportJobRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class ExportJobService {

    private final ExportJobRepository exportJobRepository;
    private final ExportJobQueue exportJobQueue;
    private final ExportProperties exportProperties;

    @Transactional
    public ExportJob create(String clientId, String requestedFileName) {
        LocalDateTime now = LocalDateTime.now();
        ExportJob job = new ExportJob(clientId, normalizeFileName(requestedFileName), now,
                now.plusHours(exportProperties.ttlHours()));
        ExportJob savedJob = exportJobRepository.save(job);
        exportJobQueue.enqueue(savedJob.getJobId());
        return savedJob;
    }

    @Transactional(readOnly = true)
    public List<ExportJob> findAllByClientId(String clientId) {
        return exportJobRepository.findAllByClientIdOrderByRequestedAtDesc(clientId);
    }

    @Transactional(readOnly = true)
    public ExportJob getByClientId(Long jobId, String clientId) {
        return exportJobRepository.findByJobIdAndClientId(jobId, clientId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Export job not found"));
    }

    @Transactional
    public ExportJob startProcessing(Long jobId) {
        ExportJob job = exportJobRepository.findById(jobId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Export job not found"));
        job.start(LocalDateTime.now());
        return job;
    }

    @Transactional
    public void complete(Long jobId, String filePath) {
        ExportJob job = exportJobRepository.findById(jobId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Export job not found"));
        job.complete(filePath, LocalDateTime.now());
    }

    @Transactional
    public void fail(Long jobId, Exception exception) {
        ExportJob job = exportJobRepository.findById(jobId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Export job not found"));
        job.fail(exception.getMessage(), LocalDateTime.now());
    }

    @Transactional
    public void failProcessingJobsOnStartup() {
        exportJobRepository.findAllByStatus(ExportJobStatus.PROCESSING)
                .forEach(job -> job.fail("Application restarted before export completed", LocalDateTime.now()));
    }

    private String normalizeFileName(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return "orders_export.xlsx";
        }

        String normalized = fileName.trim()
                .replaceAll("[\\\\/:*?\"<>|]", "_")
                .replaceAll("\\p{Cntrl}", "_");

        if (!normalized.toLowerCase().endsWith(".xlsx")) {
            normalized += ".xlsx";
        }

        return normalized;
    }
}
