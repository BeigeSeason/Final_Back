package com.springboot.final_back.service;

import com.springboot.final_back.constant.Type;
import com.springboot.final_back.dto.diary.DiarySearchListDto;
import com.springboot.final_back.entity.elasticsearch.Diary;
import com.springboot.final_back.entity.elasticsearch.TourSpots;
import com.springboot.final_back.entity.mysql.Bookmark;
import com.springboot.final_back.entity.mysql.Member;
import com.springboot.final_back.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
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
    public boolean deleteBookmark(String targetId, String userId) {
        try {
            Member member = memberRepository.findByUserId(userId).orElseThrow(() -> new RuntimeException("존재하지 않는 유저"));

            Bookmark bookmark = bookmarkRepository.findByMemberAndBookmarkedId(member, targetId).orElseThrow(() -> new RuntimeException("Bookmark not found"));

            if (bookmark.getType() == Type.DIARY) {
                Diary diary = diaryRepository.findByDiaryId(bookmark.getBookmarkedId()).orElseThrow(() -> new RuntimeException("Diary not found"));
                diary.setBookmarkCount(diary.getBookmarkCount() - 1);
                diaryRepository.save(diary);
            } else {
                log.error("여기로 오는거지?");
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

    public boolean isBookmarked(String targetId, String userId) {
        Member member = memberRepository.findByUserId(userId).orElseThrow(() -> new RuntimeException("존재하지 않는 유저"));
        return bookmarkRepository.findByMemberAndBookmarkedId(member, targetId).isPresent();
    }
}
