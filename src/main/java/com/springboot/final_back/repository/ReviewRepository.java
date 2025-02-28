package com.springboot.final_back.repository;

import com.springboot.final_back.entity.mysql.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    Optional<Review> findById(Long id);

    @Query("SELECT r.reviewedId, COUNT(r), AVG(r.rating) FROM Review r WHERE r.reviewedId IN :ids GROUP BY r.reviewedId")
    List<Object[]> findStatsByTourSpotIds(@Param("ids") List<String> tourSpotIds);

    Integer countByReviewedId(String tourSpotId);

    Double avgRatingByReviewedId(String tourSpotId);
}
