package com.springboot.final_back.repository;

import com.springboot.final_back.entity.elasticsearch.TourSpots;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TourSpotsRepository extends ElasticsearchRepository<TourSpots, Long> {
}
