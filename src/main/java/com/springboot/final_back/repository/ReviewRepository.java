package com.springboot.final_back.repository;

import com.springboot.final_back.entity.mysql.Review;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    @NotNull Optional<Review> findById(@NotNull Long id);

    @Query("SELECT r.tourSpotId, COUNT(r), AVG(r.rating) FROM Review r WHERE r.tourSpotId IN :ids GROUP BY r.tourSpotId")
    List<Object[]> findStatsByTourSpotIds(@Param("ids") List<String> tourSpotIds);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.tourSpotId = :tourSpotId")
    Integer countByTourSpotId(@Param("tourSpotId") String tourSpotId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.tourSpotId = :tourSpotId")
    Double avgRatingByTourSpotId(@Param("tourSpotId") String tourSpotId);
}