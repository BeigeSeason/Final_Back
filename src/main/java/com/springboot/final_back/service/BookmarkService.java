package com.springboot.final_back.service;

import com.springboot.final_back.constant.Type;
import com.springboot.final_back.entity.elasticsearch.Diary;
import com.springboot.final_back.entity.elasticsearch.TourSpots;
import com.springboot.final_back.entity.mysql.Bookmark;
import com.springboot.final_back.entity.mysql.Member;
import com.springboot.final_back.repository.*;
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
    private final DiaryRepository diaryRepository;

    @Transactional
    public void addBookmark(String targetId, String userId, Type type) {
        Member member = memberRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 사용자"));
        Bookmark bookmark = new Bookmark();
        bookmark.setType(type);
        bookmark.setBookmarkedId(targetId);
        bookmark.setMember(member);
        bookmarkRepository.save(bookmark);
        if (bookmark.getType() == Type.DIARY) {
            Diary diary = diaryRepository.findByDiaryId(bookmark.getBookmarkedId()).orElseThrow(() -> new RuntimeException("Diary not found"));
            diary.setBookmarkCount(diary.getBookmarkCount() + 1);
        } else {
            TourSpots tourSpot = tourSpotsRepository.findByContentId(bookmark.getBookmarkedId()).orElseThrow(() -> new RuntimeException("Diary not found"));
            tourSpot.setBookmarkCount(tourSpot.getBookmarkCount() + 1);
        }
    }

    @Transactional
    public void deleteBookmark(Long bookmarkId) {
        Bookmark bookmark = bookmarkRepository.findById(bookmarkId)
                .orElseThrow(() -> new RuntimeException("Bookmark not found"));

        if (bookmark.getType() == Type.DIARY) {
            Diary diary = diaryRepository.findByDiaryId(bookmark.getBookmarkedId()).orElseThrow(() -> new RuntimeException("Diary not found"));
            diary.setBookmarkCount(diary.getBookmarkCount() - 1);
            diaryRepository.save(diary);
        } else {
            TourSpots tourSpot = tourSpotsRepository.findByContentId(bookmark.getBookmarkedId()).orElseThrow(() -> new RuntimeException("Diary not found"));
            tourSpot.setBookmarkCount(tourSpot.getBookmarkCount() - 1);
            tourSpotsRepository.save(tourSpot);
        }
        bookmarkRepository.delete(bookmark);
    }

}
