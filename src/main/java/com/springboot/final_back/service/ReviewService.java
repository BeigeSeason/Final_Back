package com.springboot.final_back.service;

import com.springboot.final_back.entity.mysql.Review;
import com.springboot.final_back.repository.ReviewRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class ReviewService {
    private ReviewRepository reviewRepository;

    // 리뷰 입력
    // 리뷰+평점 남김 -> 게시물에 리뷰 갯수+평균 필드 추가? 이거 내일 구조 바꿔야될듯


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
