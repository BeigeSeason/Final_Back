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
    private final RedisTemplate<String, String> redisTemplate;

    private static final String REVIEW_QUEUE = "review:queue";

    // 리뷰 추가 요청
    @Async
    public void addReviewAsync(ReviewReqDto reviewReqDto) {
        String job = String.format("ADD|%s|%s|%s|%f|0",
                reviewReqDto.getTourSpotId(), reviewReqDto.getMemberId(),
                reviewReqDto.getContent(), reviewReqDto.getRating());
        redisTemplate.opsForList().leftPush(REVIEW_QUEUE, job);
        log.info("Queued review add: {}", job);
    }

    // 리뷰 수정 요청
    @Async
    public void editReviewAsync(ReviewReqDto reviewReqDto) {
        String job = String.format("EDIT|%d|%s|%f|0",
                reviewReqDto.getId(), reviewReqDto.getContent(), reviewReqDto.getRating());
        redisTemplate.opsForList().leftPush(REVIEW_QUEUE, job);
        log.info("Queued review edit: {}", job);
    }

    // 리뷰 삭제 요청
    @Async
    public void deleteReviewAsync(Long reviewId) {
        String job = String.format("DELETE|%d|0", reviewId);
        redisTemplate.opsForList().leftPush(REVIEW_QUEUE, job);
        log.info("Queued review delete: {}", job);
    }

    // 큐 처리
    @Scheduled(fixedDelay = 1000)
    @Async
    public void processReviewQueue() {
        String job = redisTemplate.opsForList().rightPop(REVIEW_QUEUE);
        if (job == null) {
            log.debug("Review queue is empty");
            return;
        }

        String[] parts = job.split("\\|");
        int retryCount = Integer.parseInt(parts[parts.length - 1]);
        if (retryCount >= 3) {
            log.error("Max retries reached, discarding review job: {}", job);
            return; // 데드 큐 이동 대신 종료
        }

        try {
            String action = parts[0];
            switch (action) {
                case "ADD":
                    addReviewFromQueue(parts[1], parts[2], parts[3], Float.parseFloat(parts[4]));
                    break;
                case "EDIT":
                    editReviewFromQueue(Long.parseLong(parts[1]), parts[2], Float.parseFloat(parts[3]));
                    break;
                case "DELETE":
                    deleteReviewFromQueue(Long.parseLong(parts[1]));
                    break;
                default:
                    log.warn("Unknown action in review job: {}", job);
                    return;
            }
            log.info("Processed review job: {}", job);
        } catch (Exception e) {
            log.error("Error processing review job: {}, retrying (count: {})", job, retryCount, e);
            String updatedJob = buildUpdatedJob(parts, retryCount);
            redisTemplate.opsForList().leftPush(REVIEW_QUEUE, updatedJob);
        }
    }

    // ADD 작업 처리
    private void addReviewFromQueue(String tourSpotId, String memberId, String content, float rating) {
        Member member = memberRepository.findByUserId(memberId)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 사용자"));
        Review review = Review.builder()
                .member(member)
                .tourSpotId(tourSpotId)
                .content(content)
                .rating(rating)
                .build();
        reviewRepository.save(review);
        updateTourSpot(tourSpotId, rating, 1);
    }

    // EDIT 작업 처리
    private void editReviewFromQueue(Long reviewId, String content, float newRating) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found"));
        float oldRating = review.getRating();
        review.setContent(content);
        review.setRating(newRating);
        reviewRepository.save(review);
        updateTourSpot(review.getTourSpotId(), newRating - oldRating, 0);
    }

    // DELETE 작업 처리
    private void deleteReviewFromQueue(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found"));
        float rating = review.getRating();
        String tourSpotId = review.getTourSpotId();
        reviewRepository.delete(review);
        updateTourSpot(tourSpotId, -rating, -1);
    }

    // 재큐잉 메시지 생성
    private String buildUpdatedJob(String[] parts, int retryCount) {
        String action = parts[0];
        return switch (action) {
            case "ADD" -> String.format("%s|%s|%s|%s|%s|%d", parts[0], parts[1], parts[2], parts[3], parts[4], retryCount + 1);
            case "EDIT" -> String.format("%s|%s|%s|%s|%d", parts[0], parts[1], parts[2], parts[3], retryCount + 1);
            case "DELETE" -> String.format("%s|%s|%d", parts[0], parts[1], retryCount + 1);
            default -> throw new IllegalArgumentException("Unknown action: " + action);
        };
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

            TourSpots spot = tourSpotsRepository.findByContentId(review.getTourSpotId()).orElseThrow(() -> new RuntimeException("존재하지 않는 여행지"));

            float newRating = spot.getRating() - review.getRating();

            spot.setReviewCount(spot.getReviewCount() - 1);
            spot.setRating(newRating);
            if (spot.getReviewCount() > 0) {
                spot.setAvgRating(newRating / spot.getReviewCount());
            } else {
                spot.setAvgRating(0); // 리뷰가 없으면 평균 점수를 0으로 설정
            }

            tourSpotsRepository.save(spot);
            return true;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}