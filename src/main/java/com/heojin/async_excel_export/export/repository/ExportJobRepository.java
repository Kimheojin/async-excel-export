package com.heojin.async_excel_export.export.repository;

import com.heojin.async_excel_export.export.domain.ExportJob;
import com.heojin.async_excel_export.export.domain.ExportJobStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExportJobRepository extends JpaRepository<ExportJob, Long> {

    List<ExportJob> findAllByClientIdOrderByRequestedAtDesc(String clientId);

    Optional<ExportJob> findByJobIdAndClientId(Long jobId, String clientId);

    List<ExportJob> findAllByStatus(ExportJobStatus status);

    List<ExportJob> findAllByExpiresAtBeforeAndFilePathIsNotNull(LocalDateTime now);
}
