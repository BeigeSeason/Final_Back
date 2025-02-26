package com.springboot.final_back.repository;

import com.springboot.final_back.entity.mysql.Ban;
import com.springboot.final_back.entity.mysql.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BanRepository extends JpaRepository<Ban, Long> {
    Ban findFirstByMemberOrderByIdDesc(Member member);

    Ban findByMember(Member member);

    List<Ban> findByIsEndFalseAndEndDateBefore(LocalDateTime endDateBefore);

    boolean existsByMemberAndEndDateIsAfter(Member member, LocalDateTime endDateAfter);
}
