package com.springboot.final_back.repository;

import com.springboot.final_back.entity.mysql.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    boolean existsByMember_UserId(String memberUserId);

    Optional<RefreshToken> findByMember_UserId(String memberUserId);
}
