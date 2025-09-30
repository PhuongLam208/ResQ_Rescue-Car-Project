package com.livewithoutthinking.resq.service.scheduler;

import com.livewithoutthinking.resq.entity.User;
import com.livewithoutthinking.resq.entity.UserRank;
import com.livewithoutthinking.resq.repository.UserRankRepository;
import com.livewithoutthinking.resq.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResetUserRankScheduler {

    private final UserRepository userRepository;
    private final UserRankRepository userRankRepository;

    @Transactional
    @Scheduled(cron = "0 0 0 1 * *", zone = "Asia/Ho_Chi_Minh")
    public void resetAllUserRanksMonthly() {
        log.info("Starting monthly user rank reset...");

        List<User> users = userRepository.findAll();

        for (User user : users) {
            int points = user.getLoyaltyPoint();
            String rank;
            int limit;

            if (points >= 10000) {
                rank = "Res Fire";
                limit = 10;
            } else if (points >= 5000) {
                rank = "Res Wood";
                limit = 7;
            } else if (points >= 2500) {
                rank = "Res Water";
                limit = 5;
            } else if (points >= 1000) {
                rank = "Res Metal";
                limit = 3;
            } else {
                rank = "Res Earth";
                limit = 1;
            }

            UserRank userRank = userRankRepository.findByUser(user)
                    .orElseGet(() -> {
                        UserRank newRank = new UserRank();
                        newRank.setUser(user);
                        return newRank;
                    });

            userRank.setRankName(rank);
            userRank.setChangeLimitLeft(limit);

            userRankRepository.save(userRank);
        }

        log.info("User rank reset completed.");
    }
}
