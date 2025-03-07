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

@Slf4j
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
    public boolean addReview(ReviewDto reviewDto) {
        try {
            Member member = memberRepository.findByUserId(reviewDto.getMemberId())
                    .orElseThrow(() -> new RuntimeException("존재하지 않는 사용자"));

            Review review = Review.builder()
                    .member(member)
                    .rating(reviewDto.getRating())
                    .tourSpotId(reviewDto.getTourSpotId())
                    .content(reviewDto.getContent())
                    .build();
            reviewRepository.save(review);

            TourSpots spot = tourSpotsRepository.findByContentId(review.getTourSpotId()).orElseThrow(() -> new RuntimeException("존재하지 않는 여행지"));

            spot.setReviewCount(spot.getReviewCount() + 1);
            spot.setRating(spot.getRating() + reviewDto.getRating());
            spot.setAvgRating(spot.getRating() / spot.getReviewCount());

            tourSpotsRepository.save(spot);
            return true;
        } catch (Exception e) {
            log.error("리뷰 생성 중 에러 발생");
            throw new RuntimeException(e);
        }
    }

    // 리뷰 수정
    @Transactional
    public boolean editReview(ReviewDto reviewDto) {
        try {
            Review review = reviewRepository.findById(reviewDto.getId())
                    .orElseThrow(() -> new RuntimeException("Review not found"));

            float rating = review.getRating();

            review.setRating(reviewDto.getRating());
            review.setContent(reviewDto.getContent());
            reviewRepository.save(review);

            TourSpots spot = tourSpotsRepository.findByContentId(review.getTourSpotId()).orElseThrow(() -> new RuntimeException("존재하지 않는 여행지"));

            // 기존의 리뷰 점수를 빼고 새 리뷰 점수 더하기
            float newRating = spot.getRating() - rating + reviewDto.getRating();

            spot.setRating(newRating);
            spot.setAvgRating(newRating / spot.getReviewCount());

            tourSpotsRepository.save(spot);
            return true;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // 리뷰 삭제
    @Transactional
    public boolean deleteReview(Long reviewId) {
        try {
            Review review = reviewRepository.findById(reviewId)
                    .orElseThrow(() -> new RuntimeException("Review not found"));

            reviewRepository.delete(review);

            TourSpots spot = tourSpotsRepository.findByContentId(review.getTourSpotId()).orElseThrow(() -> new RuntimeException("존재하지 않는 여행지"));

            float newRating = spot.getRating() - review.getRating();

            spot.setReviewCount(spot.getReviewCount() - 1);
            spot.setRating(newRating);
            spot.setAvgRating(newRating / (spot.getReviewCount() - 1));

            tourSpotsRepository.save(spot);
            return true;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
