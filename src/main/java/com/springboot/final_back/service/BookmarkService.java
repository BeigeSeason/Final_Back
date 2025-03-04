package com.springboot.final_back.service;

import com.springboot.final_back.constant.Type;
import com.springboot.final_back.entity.elasticsearch.TourSpots;
import com.springboot.final_back.entity.mysql.Bookmark;
import com.springboot.final_back.entity.mysql.Member;
import com.springboot.final_back.repository.BookmarkRepository;
import com.springboot.final_back.repository.MemberRepository;
import com.springboot.final_back.repository.ReviewRepository;
import com.springboot.final_back.repository.TourSpotsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BookmarkService {
    private final BookmarkRepository bookmarkRepository;
    private final TourSpotsRepository tourSpotsRepository;
    private final ReviewRepository reviewRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public void addBookmark(String targetId, String userId, Type type) {
        Member member = memberRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 사용자"));
        Bookmark bookmark = new Bookmark();
        bookmark.setType(type);
        bookmark.setBookmarkedId(targetId);
        bookmark.setMember(member);
        bookmarkRepository.save(bookmark);
        updateTourSpotStats(targetId);
    }

    @Transactional
    public void deleteBookmark(Long bookmarkId) {
        Bookmark bookmark = bookmarkRepository.findById(bookmarkId)
                .orElseThrow(() -> new RuntimeException("Bookmark not found"));
        String tourSpotId = bookmark.getBookmarkedId();
        bookmarkRepository.delete(bookmark);
        updateTourSpotStats(tourSpotId);
    }

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
