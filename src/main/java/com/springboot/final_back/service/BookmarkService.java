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

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional // 클래스 레벨 트랜잭션
public class BookmarkService {
    private final BookmarkRepository bookmarkRepository;
    private final TourSpotsRepository tourSpotsRepository;
    private final MemberRepository memberRepository;
    private final DiaryRepository diaryRepository;
    private final RedisTemplate<String, String> redisTemplate;

    private static final String BOOKMARK_QUEUE = "bookmark:queue";
    private static final String BOOKMARK_DEAD_QUEUE = "bookmark:dead";

    // 북마크 추가 요청
    @Async
    public void addBookmarkAsync(String targetId, String userId, String typeStr) {
        try {
            Type.valueOf(typeStr); // 유효성 사전 검사
            String job = String.format("ADD|%s|%s|%s|0", targetId, userId, typeStr);
            redisTemplate.opsForList().leftPush(BOOKMARK_QUEUE, job);
            log.info("Queued bookmark add: {}", job);
        } catch (IllegalArgumentException e) {
            log.error("Invalid bookmark type: {}", typeStr, e);
            throw new RuntimeException("유효하지 않은 타입: " + typeStr);
        }
    }

    // 북마크 삭제 요청
    @Async
    public void deleteBookmarkAsync(String targetId, String userId) {
        String job = String.format("DELETE|%s|%s|0", targetId, userId);
        redisTemplate.opsForList().leftPush(BOOKMARK_QUEUE, job);
        log.info("Queued bookmark delete: {}", job);
    }

    // 큐 처리
    @Scheduled(fixedDelay = 1000)
    @Async
    public void processBookmarkQueue() {
        String job = redisTemplate.opsForList().rightPop(BOOKMARK_QUEUE);
        if (job == null) {
            log.debug("Bookmark queue is empty");
            return;
        }

        String[] parts = job.split("\\|");
        int retryCount = Integer.parseInt(parts[parts.length - 1]);
        if (retryCount >= 3) {
            log.error("Max retries reached for bookmark job: {}", job);
            redisTemplate.opsForList().leftPush(BOOKMARK_DEAD_QUEUE, job); // 데드 큐로 이동
            return;
        }

        try {
            String action = parts[0];
            String targetId = parts[1];
            String userId = parts[2];

            Member member = memberRepository.findByUserId(userId)
                    .orElseThrow(() -> new RuntimeException("존재하지 않는 사용자"));

            switch (action) {
                case "ADD":
                    Type type = Type.valueOf(parts[3]);
                    addBookmark(member, targetId, type);
                    break;
                case "DELETE":
                    deleteBookmark(member, targetId);
                    break;
                default:
                    log.warn("Unknown action in bookmark job: {}", job);
                    return;
            }
            log.info("Processed bookmark job: {}", job);
        } catch (Exception e) {
            log.error("Error processing bookmark job: {}, retrying (count: {})", job, retryCount, e);
            String updatedJob = buildUpdatedJob(parts, retryCount);
            redisTemplate.opsForList().leftPush(BOOKMARK_QUEUE, updatedJob);
        }
    }

    // 북마크 추가
    private void addBookmark(Member member, String targetId, Type type) {
        Bookmark bookmark = Bookmark.builder()
                .type(type)
                .member(member)
                .bookmarkedId(targetId)
                .build();
        bookmarkRepository.save(bookmark);
        updateBookmarkCount(targetId, type, 1);
    }

    // 북마크 삭제
    private void deleteBookmark(Member member, String targetId) {
        Bookmark bookmark = bookmarkRepository.findByMemberAndBookmarkedId(member, targetId)
                .orElseThrow(() -> new RuntimeException("Bookmark not found"));
        bookmarkRepository.delete(bookmark);
        updateBookmarkCount(targetId, bookmark.getType(), -1);
    }

    // 재큐잉 메시지 생성
    private String buildUpdatedJob(String[] parts, int retryCount) {
        String action = parts[0];
        return switch (action) {
            case "ADD" -> String.format("%s|%s|%s|%s|%d", parts[0], parts[1], parts[2], parts[3], retryCount + 1);
            case "DELETE" -> String.format("%s|%s|%s|%d", parts[0], parts[1], parts[2], retryCount + 1);
            default -> throw new IllegalArgumentException("Unknown action: " + action);
        };
    }

    // 북마크 카운트 업데이트
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

    // 북마크 여부 확인
    public boolean isBookmarked(String targetId, String userId) {
        Member member = memberRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 유저"));
        return bookmarkRepository.findByMemberAndBookmarkedId(member, targetId).isPresent();
    }
}