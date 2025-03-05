package com.springboot.final_back.repository;

import com.springboot.final_back.constant.State;
import com.springboot.final_back.constant.Type;
import com.springboot.final_back.entity.mysql.Report;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {
    Optional<Report> findById(Long id);

    Page<Report> findAllByReportType(Pageable pageable, Type reportType);

    Page<Report> findAllByReportTypeAndState(Pageable pageable, Type reportType, State state);

    Page<Report> findAll(Pageable pageable);

    Page<Report> findAllByState(String wait, PageRequest of);

    // 월별 신고 통계
    @Query("SELECT MONTH(r.createdAt), COUNT(r) " +
            "FROM Report r " +
            "WHERE YEAR(r.createdAt) = :year " +
            "GROUP BY MONTH(r.createdAt) " +
            "ORDER BY MONTH(r.createdAt)")
    List<Object[]> getMonthlyReportCounts(@Param("year") int year);
}
