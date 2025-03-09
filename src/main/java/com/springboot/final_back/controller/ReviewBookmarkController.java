package com.springboot.final_back.controller;


import com.springboot.final_back.dto.ReviewReqDto;
import com.springboot.final_back.dto.ReviewResDto;
import com.springboot.final_back.service.BookmarkService;
import com.springboot.final_back.service.ReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/review-bookmark")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class ReviewBookmarkController {
    private final ReviewService reviewService;
    private final BookmarkService bookmarkService;

    // 리뷰 작성
    @PostMapping("/add-review")
    public ResponseEntity<Boolean> addReview(@RequestBody ReviewReqDto reviewReqDto) {
        return new ResponseEntity<>(reviewService.addReview(reviewReqDto), HttpStatus.OK);
    }

    // 리뷰 수정
    @PostMapping("/edit-review")
    public ResponseEntity<Boolean> editReview(@RequestBody ReviewReqDto reviewReqDto) {
        return new ResponseEntity<>(reviewService.editReview(reviewReqDto), HttpStatus.OK);
    }

    // 리뷰 삭제
    @PostMapping("/delete-review")
    public ResponseEntity<Boolean> deleteReview(@RequestParam Long reviewId) {
        return new ResponseEntity<>(reviewService.deleteReview(reviewId), HttpStatus.OK);
    }

    // 북마크 추가
    @PostMapping("/add-bookmark")
    public ResponseEntity<Boolean> addBookmark(@RequestParam String targetId,
                                               @RequestParam String userId,
                                               @RequestParam String type) {
        return new ResponseEntity<>(bookmarkService.addBookmark(targetId, userId, type), HttpStatus.OK);
    }

    // 북마크 삭제
    @PostMapping("/delete-bookmark")
    public ResponseEntity<Boolean> deleteBookmark(@RequestParam String targetId, @RequestParam String userId) {
        return new ResponseEntity<>(bookmarkService.deleteBookmark(targetId, userId), HttpStatus.OK);
    }

    // 내가 북마크 여부 조회
    @GetMapping("/my-bookmark")
    public ResponseEntity<Boolean> isBookmarked(@RequestParam String targetId, @RequestParam String userId) {
        return ResponseEntity.ok(bookmarkService.isBookmarked(targetId, userId));
    }


    // 리뷰 조회
    @GetMapping("/review-list")
    public ResponseEntity<Page<ReviewResDto>> getReviews(@RequestParam int page,
                                                         @RequestParam int size,
                                                         @RequestParam String tourSpotId) {
        return new ResponseEntity<>(reviewService.getReviews(page, size, tourSpotId), HttpStatus.OK);
    }
}
