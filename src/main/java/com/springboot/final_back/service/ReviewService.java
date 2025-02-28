package com.springboot.final_back.service;

import com.springboot.final_back.dto.ReviewDto;
import com.springboot.final_back.entity.mysql.Member;
import com.springboot.final_back.entity.mysql.Review;
import com.springboot.final_back.repository.MemberRepository;
import com.springboot.final_back.repository.ReviewRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class ReviewService {
    private ReviewRepository reviewRepository;
    private MemberRepository memberRepository;
    private TourSpotService tourSpotService;

    // 리뷰 입력
    public boolean createReview(ReviewDto reviewDto) {
        try {
            Member member = memberRepository.findByUserId(reviewDto.getMemberId()).orElseThrow(() -> new RuntimeException("존재하지 않는 사용자 Id입니다."));
            Review review = Review.builder()
                    .member(member)
                    .rating(reviewDto.getRating())
                    .reviewedId(reviewDto.getReviewedId())
                    .content(reviewDto.getContent())
                    .build();

            reviewRepository.save(review);
            return true;
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
    }

    // 리뷰 수정
    public boolean editReview(Long reviewId, int rating, String content) {
        try {
            Review review = reviewRepository.findById(reviewId)
                    .orElseThrow(() -> new RuntimeException("해당 댓글을 찾을 수 없습니다."));
            review.setRating(rating);
            review.setContent(content);
            reviewRepository.save(review);
            return true;
        } catch (Exception e) {
            log.info("댓글 수정 오류: {}", e.getMessage());
            return false;
        }
    }

    // 리뷰 삭제
    public boolean deleteReview(Long reviewId) {
        try {
            Review review = reviewRepository.findById(reviewId)
                    .orElseThrow(() -> new RuntimeException("해당 댓글을 찾을 수 없습니다."));
            reviewRepository.delete(review);
            return true;
        } catch (Exception e) {
            throw new RuntimeException("댓글 삭제 오류: {}", e);
        }
    }
}
