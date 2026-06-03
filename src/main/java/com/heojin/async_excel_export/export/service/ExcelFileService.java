package com.heojin.async_excel_export.export.service;

import com.heojin.async_excel_export.dummyData.entity.Data;
import com.heojin.async_excel_export.dummyData.repository.DataRepository;
import com.heojin.async_excel_export.export.config.ExportProperties;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ExcelFileService {

    private static final String[] HEADERS = {
            "id",
            "user_name",
            "product_name",
            "category",
            "amount",
            "status",
            "order_date"
    };

    private final DataRepository dataRepository;
    private final ExportProperties exportProperties;

    @Transactional(readOnly = true)
    public String generate(Long jobId) throws IOException {
        Path storageDirectory = Path.of(exportProperties.storagePath());
        Files.createDirectories(storageDirectory);

        Path filePath = storageDirectory.resolve("orders_export_" + jobId + ".xlsx");

        try (SXSSFWorkbook workbook = new SXSSFWorkbook(100);
             OutputStream outputStream = Files.newOutputStream(filePath)) {
            Sheet sheet = workbook.createSheet("orders");
            writeHeader(sheet);
            writeRows(sheet);
            workbook.write(outputStream);
            workbook.dispose();
        }

        return filePath.toString();
    }

    private void writeHeader(Sheet sheet) {
        Row row = sheet.createRow(0);
        for (int i = 0; i < HEADERS.length; i++) {
            row.createCell(i).setCellValue(HEADERS[i]);
        }
    }

    private void writeRows(Sheet sheet) {
        int rowIndex = 1;
        int pageNumber = 0;
        Page<Data> page;

        do {
            PageRequest pageRequest = PageRequest.of(
                    pageNumber,
                    exportProperties.pageSize(),
                    Sort.by(Sort.Direction.ASC, "id")
            );
            page = dataRepository.findAll(pageRequest);

            for (Data data : page.getContent()) {
                Row row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(data.getId());
                row.createCell(1).setCellValue(data.getUserName());
                row.createCell(2).setCellValue(data.getProductName());
                row.createCell(3).setCellValue(data.getCategory());
                row.createCell(4).setCellValue(data.getAmount());
                row.createCell(5).setCellValue(data.getStatus().name());
                row.createCell(6).setCellValue(data.getOrderDate().toString());
            }

            pageNumber++;
        } while (page.hasNext());
    }
}
