package com.heojin.async_excel_export.export.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.heojin.async_excel_export.export.config.ExportProperties;
import com.heojin.async_excel_export.export.config.JobQueue.ExportJobQueue;
import com.heojin.async_excel_export.export.domain.ExportJob;
import com.heojin.async_excel_export.export.domain.ExportJobStatus;
import com.heojin.async_excel_export.export.repository.ExportJobRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@ExtendWith(MockitoExtension.class)
class ExportJobServiceTest {

    @Mock
    private ExportJobRepository exportJobRepository;

    @Mock
    private ExportJobQueue exportJobQueue;

    private final ExportProperties exportProperties = new ExportProperties("/tmp/export-test", 1, 1000);

    @AfterEach
    void tearDown() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    void createEnqueuesJobAfterCommit() {
        ExportJob savedJob = new ExportJob("client-id", "orders.xlsx", LocalDateTime.now(),
                LocalDateTime.now().plusHours(1));
        ReflectionTestUtils.setField(savedJob, "jobId", 1L);

        when(exportJobRepository.save(any(ExportJob.class))).thenReturn(savedJob);
        TransactionSynchronizationManager.initSynchronization();

        exportJobService().create("client-id", "orders.xlsx");

        verify(exportJobQueue, never()).enqueue(1L);

        TransactionSynchronizationManager.getSynchronizations()
                .forEach(TransactionSynchronization::afterCommit);

        verify(exportJobQueue).enqueue(1L);
    }

    @Test
    void createUsesTimestampedDefaultFileNameWhenRequestFileNameIsNull() {
        when(exportJobRepository.save(any(ExportJob.class))).thenAnswer(invocation -> invocation.getArgument(0));

        exportJobService().create("client-id", null);

        ArgumentCaptor<ExportJob> jobCaptor = ArgumentCaptor.forClass(ExportJob.class);
        verify(exportJobRepository).save(jobCaptor.capture());

        assertThat(jobCaptor.getValue().getRequestedFileName())
                .matches("orders_export-\\d{8}-\\d{6}\\.xlsx");
    }

    @Test
    void createUsesTimestampedDefaultFileNameWhenRequestFileNameIsBlank() {
        when(exportJobRepository.save(any(ExportJob.class))).thenAnswer(invocation -> invocation.getArgument(0));

        exportJobService().create("client-id", " ");

        ArgumentCaptor<ExportJob> jobCaptor = ArgumentCaptor.forClass(ExportJob.class);
        verify(exportJobRepository).save(jobCaptor.capture());

        assertThat(jobCaptor.getValue().getRequestedFileName())
                .matches("orders_export-\\d{8}-\\d{6}\\.xlsx");
    }

    @Test
    void createKeepsRequestedFileNameWhenItAlreadyHasXlsxExtension() {
        when(exportJobRepository.save(any(ExportJob.class))).thenAnswer(invocation -> invocation.getArgument(0));

        exportJobService().create("client-id", "orders.xlsx");

        ArgumentCaptor<ExportJob> jobCaptor = ArgumentCaptor.forClass(ExportJob.class);
        verify(exportJobRepository).save(jobCaptor.capture());

        assertThat(jobCaptor.getValue().getRequestedFileName()).isEqualTo("orders.xlsx");
    }

    @Test
    void createAppendsXlsxExtensionToRequestedFileName() {
        when(exportJobRepository.save(any(ExportJob.class))).thenAnswer(invocation -> invocation.getArgument(0));

        exportJobService().create("client-id", "orders");

        ArgumentCaptor<ExportJob> jobCaptor = ArgumentCaptor.forClass(ExportJob.class);
        verify(exportJobRepository).save(jobCaptor.capture());

        assertThat(jobCaptor.getValue().getRequestedFileName()).isEqualTo("orders.xlsx");
    }

    @Test
    void createReplacesInvalidCharactersInRequestedFileName() {
        when(exportJobRepository.save(any(ExportJob.class))).thenAnswer(invocation -> invocation.getArgument(0));

        exportJobService().create("client-id", "orders/export:*?");

        ArgumentCaptor<ExportJob> jobCaptor = ArgumentCaptor.forClass(ExportJob.class);
        verify(exportJobRepository).save(jobCaptor.capture());

        assertThat(jobCaptor.getValue().getRequestedFileName()).isEqualTo("orders_export___.xlsx");
    }

    @Test
    void recoverJobsOnStartupReEnqueuesPendingJobsAndFailsProcessingJobs() {
        ExportJob pendingJob = new ExportJob("client-id", "pending.xlsx", LocalDateTime.now(),
                LocalDateTime.now().plusHours(1));
        ReflectionTestUtils.setField(pendingJob, "jobId", 1L);

        ExportJob processingJob = new ExportJob("client-id", "processing.xlsx", LocalDateTime.now(),
                LocalDateTime.now().plusHours(1));
        ReflectionTestUtils.setField(processingJob, "jobId", 2L);
        processingJob.start(LocalDateTime.now());

        when(exportJobRepository.findAllByStatus(ExportJobStatus.PENDING)).thenReturn(List.of(pendingJob));
        when(exportJobRepository.findAllByStatus(ExportJobStatus.PROCESSING)).thenReturn(List.of(processingJob));

        exportJobService().recoverJobsOnStartup();

        verify(exportJobQueue).enqueue(1L);
        assertThat(processingJob.getStatus()).isEqualTo(ExportJobStatus.FAILED);
        assertThat(processingJob.getErrorMessage()).isEqualTo("Application restarted before export completed");
    }

    private ExportJobService exportJobService() {
        return new ExportJobService(exportJobRepository, exportJobQueue, exportProperties);
    }
}
