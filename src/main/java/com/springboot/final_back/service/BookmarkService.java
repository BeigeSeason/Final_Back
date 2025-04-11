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

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional // 클래스 레벨 트랜잭션
public class BookmarkService {
    private final BookmarkRepository bookmarkRepository;
    private final TourSpotsRepository tourSpotsRepository;
    private final MemberRepository memberRepository;
    private final DiaryRepository diaryRepository;

    // 메모리 내 큐로 대체
    private final Queue<String> bookmarkQueue = new ConcurrentLinkedQueue<>();

    // 북마크 추가 요청
    @Async
    public void addBookmarkAsync(String targetId, String userId, String typeStr) {
        try {
            Type type = Type.valueOf(typeStr); // 유효성 사전 검사
            Member member = memberRepository.findByUserId(userId)
                    .orElseThrow(() -> new RuntimeException("존재하지 않는 사용자"));
            addBookmark(member, targetId, type); // 큐 대신 직접 호출
            log.info("Bookmark added: targetId={}, userId={}, type={}", targetId, userId, typeStr);
        } catch (IllegalArgumentException e) {
            log.error("Invalid bookmark type: {}", typeStr, e);
            throw new RuntimeException("유효하지 않은 타입: " + typeStr);
        }
    }

    // 북마크 삭제 요청
    @Async
    public void deleteBookmarkAsync(String targetId, String userId) {
        try {
            Member member = memberRepository.findByUserId(userId)
                    .orElseThrow(() -> new RuntimeException("존재하지 않는 사용자"));
            deleteBookmark(member, targetId); // 큐 대신 직접 호출
            log.info("Bookmark deleted: targetId={}, userId={}", targetId, userId);
        } catch (Exception e) {
            log.error("Error deleting bookmark: targetId={}, userId={}", targetId, userId, e);
            throw e; // 예외를 다시 던져 호출자가 처리하도록 함
        }
    }

    // 큐 처리 (옵션: 필요 시 메모리 큐를 사용할 경우)
    @Scheduled(fixedDelay = 1000)
    @Async
    public void processBookmarkQueue() {
        String job = bookmarkQueue.poll(); // 큐에서 작업 가져오기
        if (job == null) {
            log.debug("Bookmark queue is empty");
            return;
        }

        String[] parts = job.split("\\|");
        int retryCount = Integer.parseInt(parts[parts.length - 1]);
        if (retryCount >= 3) {
            log.error("Max retries reached, discarding bookmark job: {}", job);
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
            bookmarkQueue.offer(updatedJob); // 실패 시 큐에 다시 추가
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
            log.info("Bookmark count updated: {}", diary.getBookmarkCount());
            diaryRepository.save(diary);
        } else {
            TourSpots tourSpot = tourSpotsRepository.findByContentId(targetId)
                    .orElseThrow(() -> new RuntimeException("Tour spot not found"));
            tourSpot.setBookmarkCount(tourSpot.getBookmarkCount() + delta);
            log.info("Bookmark count updated: {}", tourSpot.getBookmarkCount());
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