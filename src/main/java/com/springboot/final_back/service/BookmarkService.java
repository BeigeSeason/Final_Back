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
    public boolean addBookmark(String targetId, String userId, String typeStr) {
        try {
            Member member = memberRepository.findByUserId(userId)
                    .orElseThrow(() -> new RuntimeException("존재하지 않는 사용자"));
            Type type = Type.valueOf(typeStr);

            Bookmark bookmark = Bookmark.builder()
                    .type(type)
                    .member(member)
                    .bookmarkedId(targetId)
                    .build();

            bookmarkRepository.save(bookmark);
            if (bookmark.getType() == Type.DIARY) {
                Diary diary = diaryRepository.findByDiaryId(bookmark.getBookmarkedId()).orElseThrow(() -> new RuntimeException("Diary not found"));
                diary.setBookmarkCount(diary.getBookmarkCount() + 1);

                diaryRepository.save(diary);
            } else {
                TourSpots tourSpot = tourSpotsRepository.findByContentId(bookmark.getBookmarkedId()).orElseThrow(() -> new RuntimeException("Diary not found"));
                tourSpot.setBookmarkCount(tourSpot.getBookmarkCount() + 1);
                tourSpotsRepository.save(tourSpot);
            }

            return true;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }


    }

    @Transactional
    public boolean deleteBookmark(Long bookmarkId) {
        try {
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

            return true;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
