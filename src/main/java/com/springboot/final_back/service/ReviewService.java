package com.springboot.final_back.service;

import com.springboot.final_back.dto.ReviewReqDto;
import com.springboot.final_back.dto.ReviewResDto;
import com.springboot.final_back.entity.elasticsearch.TourSpots;
import com.springboot.final_back.entity.mysql.Member;
import com.springboot.final_back.entity.mysql.Review;
import com.springboot.final_back.repository.MemberRepository;
import com.springboot.final_back.repository.ReviewRepository;
import com.springboot.final_back.repository.TourSpotsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional // 클래스 레벨 트랜잭션
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final TourSpotsRepository tourSpotsRepository;
    private final MemberRepository memberRepository;

    // 리뷰 추가 요청
    @Async
    public void addReviewAsync(ReviewReqDto reviewReqDto) {
        try {
            Member member = memberRepository.findByUserId(reviewReqDto.getMemberId())
                    .orElseThrow(() -> new RuntimeException("존재하지 않는 사용자"));
            Review review = Review.builder()
                    .member(member)
                    .tourSpotId(reviewReqDto.getTourSpotId())
                    .content(reviewReqDto.getContent())
                    .rating(reviewReqDto.getRating())
                    .build();
            reviewRepository.save(review);
            updateTourSpot(reviewReqDto.getTourSpotId(), reviewReqDto.getRating(), 1);
            log.info("Review added: tourSpotId={}, memberId={}", reviewReqDto.getTourSpotId(), reviewReqDto.getMemberId());
        } catch (Exception e) {
            log.error("Error adding review: {}", reviewReqDto, e);
            throw e;
        }
    }

    // 리뷰 수정 요청
    @Async
    public void editReviewAsync(ReviewReqDto reviewReqDto) {
        try {
            Review review = reviewRepository.findById(reviewReqDto.getId())
                    .orElseThrow(() -> new RuntimeException("Review not found"));
            float oldRating = review.getRating();
            review.setContent(reviewReqDto.getContent());
            review.setRating(reviewReqDto.getRating());
            reviewRepository.save(review);
            updateTourSpot(review.getTourSpotId(), reviewReqDto.getRating() - oldRating, 0);
            log.info("Review edited: id={}", reviewReqDto.getId());
        } catch (Exception e) {
            log.error("Error editing review: {}", reviewReqDto, e);
            throw e;
        }
    }

    // 리뷰 삭제 요청
    @Async
    public void deleteReviewAsync(Long reviewId) {
        try {
            Review review = reviewRepository.findById(reviewId)
                    .orElseThrow(() -> new RuntimeException("Review not found"));
            float rating = review.getRating();
            String tourSpotId = review.getTourSpotId();
            reviewRepository.delete(review);
            updateTourSpot(tourSpotId, -rating, -1);
            log.info("Review deleted: id={}", reviewId);
        } catch (Exception e) {
            log.error("Error deleting review: id={}", reviewId, e);
            throw e;
        }
    }

    // TourSpots 업데이트
    private void updateTourSpot(String tourSpotId, float ratingDelta, int countDelta) {
        TourSpots spot = tourSpotsRepository.findByContentId(tourSpotId)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 여행지"));
        float newRating = spot.getRating() + ratingDelta;
        int newCount = spot.getReviewCount() + countDelta;
        spot.setRating(newRating);
        spot.setReviewCount(newCount);
        spot.setAvgRating(newCount > 0 ? newRating / newCount : 0);
        tourSpotsRepository.save(spot);
    }

    // 리뷰 조회
    public Page<ReviewResDto> getReviews(int page, int size, String tourSpotId) {
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Review> reviews = reviewRepository.findAllByTourSpotId(tourSpotId, pageable);

        List<ReviewResDto> reviewResDtoList = reviews.stream()
                .map(review -> {
                    Member member = review.getMember();
                    return ReviewResDto.builder()
                            .id(review.getId())
                            .memberId(member.getUserId())
                            .nickname(member.getNickname())
                            .profileImg(member.getImgPath())
                            .createdAt(review.getCreatedAt())
                            .rating(review.getRating())
                            .content(review.getContent())
                            .build();
                })
                .toList();

        return new PageImpl<>(reviewResDtoList, pageable, reviews.getTotalElements());
    }

    // 내가 작성한 리뷰 조회
    public Page<ReviewResDto> getMyReviews(int page, int size, String userId) {
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        Pageable pageable = PageRequest.of(page, size, sort);

        Member member = memberRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 사용자"));

        Page<Review> reviews = reviewRepository.findAllByMember(member, pageable);

        List<ReviewResDto> reviewResDtoList = reviews.stream()
                .map(review -> {
                    TourSpots tourSpot = tourSpotsRepository.findByContentId(review.getTourSpotId())
                            .orElseThrow(() -> new RuntimeException("Tour spot not found"));
                    return ReviewResDto.builder()
                            .id(review.getId())
                            .memberId(member.getUserId())
                            .nickname(member.getNickname())
                            .profileImg(member.getImgPath())
                            .createdAt(review.getCreatedAt())
                            .rating(review.getRating())
                            .content(review.getContent())
                            .tourspotId(tourSpot.getContentId())
                            .tourspotTitle(tourSpot.getTitle())
                            .build();
                })
                .collect(Collectors.toList());

        return new PageImpl<>(reviewResDtoList, pageable, reviews.getTotalElements());
    }

    // 리뷰 삭제
    @Transactional
    public boolean deleteReview(Long reviewId) {
        try {
            Review review = reviewRepository.findById(reviewId)
                    .orElseThrow(() -> new RuntimeException("Review not found"));

            reviewRepository.delete(review);

            TourSpots spot = tourSpotsRepository.findByContentId(review.getTourSpotId())
                    .orElseThrow(() -> new RuntimeException("존재하지 않는 여행지"));

            float newRating = spot.getRating() - review.getRating();
            spot.setReviewCount(spot.getReviewCount() - 1);
            spot.setRating(newRating);
            spot.setAvgRating(spot.getReviewCount() > 0 ? newRating / spot.getReviewCount() : 0);

            tourSpotsRepository.save(spot);
            return true;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}