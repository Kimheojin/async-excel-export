package com.heojin.async_excel_export.export.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.export")
public record ExportProperties(
        String storagePath,
        long ttlHours,
        int pageSize
) {
}
