package com.heojin.async_excel_export.export.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.heojin.async_excel_export.export.domain.ExportJob;
import com.heojin.async_excel_export.export.domain.ExportJobStatus;
import java.time.LocalDateTime;

public record ExportJobResponse(
        @JsonProperty("job_id")
        Long jobId,
        @JsonProperty("file_name")
        String fileName,
        ExportJobStatus status,
        @JsonProperty("requested_at")
        LocalDateTime requestedAt,
        @JsonProperty("started_at")
        LocalDateTime startedAt,
        @JsonProperty("completed_at")
        LocalDateTime completedAt,
        @JsonProperty("file_path")
        String filePath
) {

    public static ExportJobResponse from(ExportJob job) {
        return new ExportJobResponse(
                job.getJobId(),
                job.getRequestedFileName(),
                job.getStatus(),
                job.getRequestedAt(),
                job.getStartedAt(),
                job.getCompletedAt(),
                job.getFilePath()
        );
    }
}
