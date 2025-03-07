package com.springboot.final_back.controller;


import com.springboot.final_back.constant.Type;
import com.springboot.final_back.dto.ReviewDto;
import com.springboot.final_back.entity.mysql.Bookmark;
import com.springboot.final_back.entity.mysql.Review;
import com.springboot.final_back.repository.BookmarkRepository;
import com.springboot.final_back.repository.ReviewRepository;
import com.springboot.final_back.service.BookmarkService;
import com.springboot.final_back.service.ReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    @PostMapping("/add-review")
    public ResponseEntity<Boolean> addReview(@RequestBody ReviewDto reviewDto) {
        return new ResponseEntity<>(reviewService.addReview(reviewDto), HttpStatus.OK);
    }

    @PostMapping("/edit-review")
    public ResponseEntity<Boolean> editReview(@RequestBody ReviewDto reviewDto) {
        return new ResponseEntity<>(reviewService.editReview(reviewDto), HttpStatus.OK);
    }

    @PostMapping("/delete-review")
    public ResponseEntity<Boolean> deleteReview(@RequestParam Long reviewId) {
        return new ResponseEntity<>(reviewService.deleteReview(reviewId), HttpStatus.OK);
    }


    @PostMapping("/add-bookmark")
    public ResponseEntity<Boolean> addBookmark(@RequestParam String target,
                                               @RequestParam String userId,
                                               @RequestParam String type) {
        return new ResponseEntity<>(bookmarkService.addBookmark(target, userId, type), HttpStatus.OK);
    }

    @PostMapping("/delete-bookmark")
    public ResponseEntity<Boolean> deleteBookmark(@RequestParam Long reviewId) {
        return new ResponseEntity<>(bookmarkService.deleteBookmark(reviewId), HttpStatus.OK);
    }
}
