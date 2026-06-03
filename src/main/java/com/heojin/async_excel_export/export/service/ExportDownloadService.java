package com.heojin.async_excel_export.export.service;

import com.heojin.async_excel_export.export.domain.ExportJob;
import com.heojin.async_excel_export.export.domain.ExportJobStatus;
import java.nio.file.Files;
import java.nio.file.Path;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class ExportDownloadService {

    private final ExportJobService exportJobService;

    @Transactional(readOnly = true)
    public DownloadFile getDownloadFile(Long jobId, String clientId) {
        ExportJob job = exportJobService.getByClientId(jobId, clientId);

        if (job.getStatus() != ExportJobStatus.DONE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Export job is not completed");
        }

        if (job.getFilePath() == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Export file is expired or deleted");
        }

        Path filePath = Path.of(job.getFilePath());
        if (!Files.exists(filePath)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Export file not found");
        }

        return new DownloadFile(new FileSystemResource(filePath), job.getRequestedFileName());
    }

    public record DownloadFile(Resource resource, String fileName) {
    }
}
