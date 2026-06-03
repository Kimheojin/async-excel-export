package com.heojin.async_excel_export.export.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ExportJob {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "job_id")
    private Long jobId;

    @Column(name = "client_id", nullable = false, length = 36)
    private String clientId;

    @Column(name = "requested_file_name", nullable = false)
    private String requestedFileName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExportJobStatus status;

    @Column(name = "requested_at", nullable = false)
    private LocalDateTime requestedAt;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "file_path")
    private String filePath;

    @Column(name = "error_message", length = 2000)
    private String errorMessage;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    public ExportJob(String clientId, String requestedFileName, LocalDateTime requestedAt, LocalDateTime expiresAt) {
        this.clientId = clientId;
        this.requestedFileName = requestedFileName;
        this.status = ExportJobStatus.PENDING;
        this.requestedAt = requestedAt;
        this.expiresAt = expiresAt;
    }

    public void start(LocalDateTime startedAt) {
        this.status = ExportJobStatus.PROCESSING;
        this.startedAt = startedAt;
        this.errorMessage = null;
    }

    public void complete(String filePath, LocalDateTime completedAt) {
        this.status = ExportJobStatus.DONE;
        this.filePath = filePath;
        this.completedAt = completedAt;
        this.errorMessage = null;
    }

    public void fail(String errorMessage, LocalDateTime completedAt) {
        this.status = ExportJobStatus.FAILED;
        this.errorMessage = errorMessage;
        this.completedAt = completedAt;
    }

    public void clearFilePath() {
        this.filePath = null;
    }
}
