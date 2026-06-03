package com.heojin.async_excel_export.export.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.heojin.async_excel_export.export.domain.ExportJob;
import com.heojin.async_excel_export.export.domain.ExportJobStatus;

public record ExportJobCreateResponse(
        @JsonProperty("job_id")
        Long jobId,
        @JsonProperty("file_name")
        String fileName,
        ExportJobStatus status
) {

    public static ExportJobCreateResponse from(ExportJob job) {
        return new ExportJobCreateResponse(job.getJobId(), job.getRequestedFileName(), job.getStatus());
    }
}
