package com.heojin.async_excel_export;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@ConfigurationPropertiesScan
@SpringBootApplication
public class AsyncExcelExportApplication {

	public static void main(String[] args) {
		SpringApplication.run(AsyncExcelExportApplication.class, args);
	}

}
