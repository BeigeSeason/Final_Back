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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final TourSpotsRepository tourSpotsRepository;
    private final MemberRepository memberRepository;

    // 리뷰 작성
    @Transactional
    public boolean addReview(ReviewReqDto reviewReqDto) {
        try {
            Member member = memberRepository.findByUserId(reviewReqDto.getMemberId())
                    .orElseThrow(() -> new RuntimeException("존재하지 않는 사용자"));

            Review review = Review.builder()
                    .member(member)
                    .rating(reviewReqDto.getRating())
                    .tourSpotId(reviewReqDto.getTourSpotId())
                    .content(reviewReqDto.getContent())
                    .build();
            reviewRepository.save(review);

            TourSpots spot = tourSpotsRepository.findByContentId(review.getTourSpotId()).orElseThrow(() -> new RuntimeException("존재하지 않는 여행지"));

            spot.setReviewCount(spot.getReviewCount() + 1);
            spot.setRating(spot.getRating() + reviewReqDto.getRating());
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
    public boolean editReview(ReviewReqDto reviewReqDto) {
        try {
            Review review = reviewRepository.findById(reviewReqDto.getId())
                    .orElseThrow(() -> new RuntimeException("Review not found"));

            float rating = review.getRating();

            review.setRating(reviewReqDto.getRating());
            review.setContent(reviewReqDto.getContent());
            reviewRepository.save(review);

            TourSpots spot = tourSpotsRepository.findByContentId(review.getTourSpotId()).orElseThrow(() -> new RuntimeException("존재하지 않는 여행지"));

            // 기존의 리뷰 점수를 빼고 새 리뷰 점수 더하기
            float newRating = spot.getRating() - rating + reviewReqDto.getRating();

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

    public Page<ReviewResDto> getReviews(int page, int size, String tourSpotId) {
        // 정렬 기준: createdAt 내림차순
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        // Pageable 객체 생성
        Pageable pageable = PageRequest.of(page, size, sort);
        // Repository에서 Review 조회
        Page<Review> reviews = reviewRepository.findAllByTourSpotId(tourSpotId, pageable);

        // Review 리스트를 ReviewResDto 리스트로 변환
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

        // Page 객체로 변환하여 반환
        return new PageImpl<>(reviewResDtoList, pageable, reviews.getTotalElements());
    }

}
