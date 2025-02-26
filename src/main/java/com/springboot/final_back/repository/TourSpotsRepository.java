package com.springboot.final_back.repository;

import com.springboot.final_back.entity.elasticsearch.TourSpots;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TourSpotsRepository extends ElasticsearchRepository<TourSpots, Long> {
    Page<TourSpots> findByTitleOrAddr1(String title, String addr1, Pageable pageable);

    Optional<TourSpots> findByContentId(String contentId);
}
