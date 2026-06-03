package com.heojin.async_excel_export.export.config.JobQueue;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import org.springframework.stereotype.Component;

@Component
public class ExportJobQueue {

    private final BlockingQueue<Long> queue = new LinkedBlockingQueue<>();

    public void enqueue(Long jobId) {
        queue.offer(jobId);
    }

    public Long take() throws InterruptedException {
        return queue.take();
    }
}
