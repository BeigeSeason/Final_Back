package com.springboot.final_back.service;

import com.springboot.final_back.constant.Type;
import com.springboot.final_back.entity.elasticsearch.Diary;
import com.springboot.final_back.entity.elasticsearch.TourSpots;
import com.springboot.final_back.entity.mysql.Bookmark;
import com.springboot.final_back.entity.mysql.Member;
import com.springboot.final_back.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestParam;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookmarkService {
    private final BookmarkRepository bookmarkRepository;
    private final TourSpotsRepository tourSpotsRepository;
    private final MemberRepository memberRepository;
    private final DiaryRepository diaryRepository;

    private final RedisTemplate<String, String> redisTemplate;

    private static final String BOOKMARK_QUEUE = "bookmark:queue";

    // 북마크 추가 요청을 큐에 넣음
    public void addBookmarkAsync(String targetId, String userId, String typeStr) {
        String job = String.format("ADD|%s|%s|%s", targetId, userId, typeStr);
        redisTemplate.opsForList().leftPush(BOOKMARK_QUEUE, job);
        log.info("Queued bookmark add: {}", job);
    }

    // 북마크 삭제 요청을 큐에 넣음
    public void deleteBookmarkAsync(String targetId, String userId) {
        String job = String.format("DELETE|%s|%s", targetId, userId);
        redisTemplate.opsForList().leftPush(BOOKMARK_QUEUE, job);
        log.info("Queued bookmark delete: {}", job);
    }

    // 워커: 큐에서 작업 처리 (주기적으로 실행)
    @Scheduled(fixedDelay = 1000) // 1초마다 실행
    @Async // 비동기 실행
    @Transactional
    public void processBookmarkQueue() {
        String job = redisTemplate.opsForList().rightPop(BOOKMARK_QUEUE);
        if (job == null) return;

        try {
            String[] parts = job.split("\\|");
            String action = parts[0];
            String targetId = parts[1];
            String userId = parts[2];

            Member member = memberRepository.findByUserId(userId)
                    .orElseThrow(() -> new RuntimeException("존재하지 않는 사용자"));

            if ("ADD".equals(action)) {
                Type type = Type.valueOf(parts[3]);
                Bookmark bookmark = Bookmark.builder()
                        .type(type)
                        .member(member)
                        .bookmarkedId(targetId)
                        .build();
                bookmarkRepository.save(bookmark);
                updateBookmarkCount(targetId, type, 1);
            } else if ("DELETE".equals(action)) {
                Bookmark bookmark = bookmarkRepository.findByMemberAndBookmarkedId(member, targetId)
                        .orElseThrow(() -> new RuntimeException("Bookmark not found"));
                bookmarkRepository.delete(bookmark);
                updateBookmarkCount(targetId, bookmark.getType(), -1);
            }
            log.info("Processed bookmark job: {}", job);
        } catch (Exception e) {
            log.error("Error processing bookmark job: {}", job, e);
            // 실패 시 재큐잉 또는 별도 실패 큐로 이동
            redisTemplate.opsForList().leftPush("bookmark:failed", job);
        }
    }

    private void updateBookmarkCount(String targetId, Type type, int delta) {
        if (type == Type.DIARY) {
            Diary diary = diaryRepository.findByDiaryId(targetId)
                    .orElseThrow(() -> new RuntimeException("Diary not found"));
            diary.setBookmarkCount(diary.getBookmarkCount() + delta);
            diaryRepository.save(diary);
        } else {
            TourSpots tourSpot = tourSpotsRepository.findByContentId(targetId)
                    .orElseThrow(() -> new RuntimeException("Tour spot not found"));
            tourSpot.setBookmarkCount(tourSpot.getBookmarkCount() + delta);
            tourSpotsRepository.save(tourSpot);
        }
    }


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
