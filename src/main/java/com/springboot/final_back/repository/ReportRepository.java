package com.springboot.final_back.repository;

import com.springboot.final_back.constant.Type;
import com.springboot.final_back.entity.mysql.Report;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {
    Optional<Report> findById(Long id);

    Page<Report> findAllByReportType(Pageable pageable, Type reportType);

}
