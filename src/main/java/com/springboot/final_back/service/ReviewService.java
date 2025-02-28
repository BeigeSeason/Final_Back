package com.springboot.final_back.service;

import com.springboot.final_back.dto.ReviewDto;
import com.springboot.final_back.entity.elasticsearch.TourSpots;
import com.springboot.final_back.entity.mysql.Member;
import com.springboot.final_back.entity.mysql.Review;
import com.springboot.final_back.repository.BookmarkRepository;
import com.springboot.final_back.repository.MemberRepository;
import com.springboot.final_back.repository.ReviewRepository;
import com.springboot.final_back.repository.TourSpotsRepository;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final TourSpotsRepository tourSpotsRepository;
    private final ElasticsearchOperations elasticsearchOperations;
    private final BookmarkRepository bookmarkRepository;
    private final MemberRepository memberRepository;

    // 리뷰 작성
    @Transactional
    public ReviewDto addReview(ReviewDto reviewDto) {
        Member member = memberRepository.findByUserId(reviewDto.getMemberId())
                .orElseThrow(() -> new RuntimeException("존재하지 않는 사용자"));

        Review review = Review.builder()
                .member(member)
                .rating(reviewDto.getRating())
                .tourSpotId(reviewDto.getTourSpotId())
                .content(reviewDto.getContent())
                .build();
        reviewRepository.save(review);
        updateTourSpotStats(reviewDto.getTourSpotId());
        return reviewDto;
    }

    // 리뷰 수정
    @Transactional
    public void updateReview(Long reviewId, ReviewDto reviewDto) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found"));
        review.setRating(reviewDto.getRating());
        review.setContent(reviewDto.getContent());
        reviewRepository.save(review);
        updateTourSpotStats(review.getTourSpotId());
    }

    // 리뷰 삭제
    @Transactional
    public void deleteReview(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found"));
        String tourSpotId = review.getTourSpotId();
        reviewRepository.delete(review);
        updateTourSpotStats(tourSpotId);
    }

    // 리뷰 적용 시 도큐먼트 업데이트
    private void updateTourSpotStats(String tourSpotId) {
        TourSpots tourSpot = tourSpotsRepository.findByContentId(tourSpotId)
                .orElseThrow(() -> new RuntimeException("TourSpot not found"));
        Integer reviewCount = reviewRepository.countByTourSpotId(tourSpotId);
        Double avgRating = reviewRepository.avgRatingByTourSpotId(tourSpotId);
        Integer bookmarkCount = bookmarkRepository.countByBookmarkedId(tourSpotId);
        tourSpot.setReviewCount(reviewCount != null ? reviewCount : 0);
        tourSpot.setRating(avgRating != null ? avgRating : 0.0);
        tourSpot.setBookmarkCount(bookmarkCount != null ? bookmarkCount : 0);
        tourSpotsRepository.save(tourSpot);
    }
}
