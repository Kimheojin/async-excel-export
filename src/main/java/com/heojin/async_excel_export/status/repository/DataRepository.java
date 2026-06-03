package com.heojin.async_excel_export.status.repository;

import com.heojin.async_excel_export.status.entity.Data;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DataRepository extends JpaRepository<Data, Long> {
}
