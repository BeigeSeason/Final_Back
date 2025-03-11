package com.springboot.final_back.schedule;

import com.springboot.final_back.constant.MemberRole;
import com.springboot.final_back.entity.mysql.Ban;
import com.springboot.final_back.entity.mysql.Member;
import com.springboot.final_back.repository.BanRepository;
import com.springboot.final_back.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class BanSchedule {
    private final BanRepository banRepository;
    private final MemberRepository memberRepository;

    @Scheduled(cron = "0 0 6 * * *", zone = "Asia/Seoul")
    @Transactional
    public void memberUnbanSchedule() {
        log.info("memberUnbanSchedule");
        try {
            LocalDateTime now = LocalDateTime.now();

            List<Ban> expiredBans = banRepository.findByIsEndFalseAndEndDateBefore(now);

            List<Ban> updatedBans = new ArrayList<>();
            List<Member> updatedMembers = new ArrayList<>();

            for (Ban ban : expiredBans) {
                boolean isLeft = banRepository.existsByMemberAndEndDateIsAfter(ban.getMember(), now);
                ban.setEnd(true);
                updatedBans.add(ban);

                if (!isLeft) {
                    Optional<Member> optionalMember = memberRepository.findById(ban.getMember().getId());
                    if (optionalMember.isPresent()) {
                        Member member = optionalMember.get();
                        member.setRole(MemberRole.USER);
                        member.setBanned(false);
                        updatedMembers.add(member);
                    } else {
                        log.warn("해당 멤버를 찾을 수 없습니다: {}", ban.getMember().getId());

                    }
                }
                banRepository.saveAll(updatedBans);
                memberRepository.saveAll(updatedMembers);
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }
}
