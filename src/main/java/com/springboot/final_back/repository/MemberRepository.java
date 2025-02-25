package com.springboot.final_back.repository;

import com.springboot.final_back.entity.mysql.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByUserId(String userId);

    boolean existsMemberByUserId(String userId);

    Page<Member> findByUserIdContaining(String userId, Pageable pageable);
    Page<Member> findByNameContaining(String name, Pageable pageable);
    Page<Member> findByNicknameContaining(String nickname, Pageable pageable);
    Page<Member> findByEmailContaining(String email, Pageable pageable);
}
