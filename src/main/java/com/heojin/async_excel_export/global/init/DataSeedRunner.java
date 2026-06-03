package com.heojin.async_excel_export.global.init;

import com.heojin.async_excel_export.dummyData.repository.DataRepository;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true")
@Profile("local")
public class DataSeedRunner implements ApplicationRunner {

    private static final String INSERT_SQL = """
            insert into data (user_name, product_name, category, amount, status, order_date)
            values (?, ?, ?, ?, ?, ?)
            """;

    private final DataRepository dataRepository;
    private final JdbcTemplate jdbcTemplate;

    @Value("${app.seed.target-count:105000}")
    private long targetCount;

    @Value("${app.seed.batch-size:1000}")
    private int batchSize;

    @Override
    public void run(ApplicationArguments args) {
        long currentCount = dataRepository.count();
        long missingCount = targetCount - currentCount;

        if (missingCount <= 0) {
            log.info("Skip data seed. currentCount={}, targetCount={}", currentCount, targetCount);
            return;
        }

        log.info("Start data seed. currentCount={}, targetCount={}, missingCount={}",
                currentCount, targetCount, missingCount);

        long insertedCount = 0;
        while (insertedCount < missingCount) {
            int currentBatchSize = (int) Math.min(batchSize, missingCount - insertedCount);
            long startIndex = currentCount + insertedCount + 1;

            jdbcTemplate.batchUpdate(INSERT_SQL, new DataSeedBatchPreparedStatementSetter(startIndex, currentBatchSize));

            insertedCount += currentBatchSize;
        }

        log.info("Complete data seed. insertedCount={}, totalCount={}", insertedCount, currentCount + insertedCount);
    }

    private static class DataSeedBatchPreparedStatementSetter implements BatchPreparedStatementSetter {

        private final long startIndex;
        private final int batchSize;
        private final LocalDateTime baseOrderDate;

        private DataSeedBatchPreparedStatementSetter(long startIndex, int batchSize) {
            this.startIndex = startIndex;
            this.batchSize = batchSize;
            this.baseOrderDate = LocalDateTime.now().minusDays(365);
        }

        @Override
        public void setValues(PreparedStatement ps, int i) throws SQLException {
            long sequence = startIndex + i;

            ps.setString(1, "user_" + sequence);
            ps.setString(2, DataSeedValues.productName(sequence));
            ps.setString(3, DataSeedValues.category(sequence));
            ps.setInt(4, DataSeedValues.amount(sequence));
            ps.setString(5, DataSeedValues.statusName(sequence));
            ps.setTimestamp(6, Timestamp.valueOf(baseOrderDate.plusMinutes(sequence)));
        }

        @Override
        public int getBatchSize() {
            return batchSize;
        }
    }
}
