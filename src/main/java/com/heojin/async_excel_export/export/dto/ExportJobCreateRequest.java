package com.heojin.async_excel_export.export.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ExportJobCreateRequest(
        @JsonProperty("file_name")
        String fileName
) {
}
