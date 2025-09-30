package com.livewithoutthinking.resq.service.scheduler;

import com.livewithoutthinking.resq.entity.Schedule;
import com.livewithoutthinking.resq.entity.Shift;
import com.livewithoutthinking.resq.entity.Staff;
import com.livewithoutthinking.resq.repository.ScheduleRepository;
import com.livewithoutthinking.resq.repository.ShiftRepository;
import com.livewithoutthinking.resq.repository.StaffRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AutoShiftCheckerService {
    private final StaffRepository staffRepository;
    private final ShiftRepository shiftRepository;
    private final ScheduleRepository scheduleRepository;

    private static final ZoneId ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    @Scheduled(cron = "0 * 8,14,20,2 * * *", zone = "Asia/Ho_Chi_Minh")
    public void autoShiftChecker() {
        LocalDateTime now = LocalDateTime.now(ZONE);
        LocalDate today = now.toLocalDate();

        List<Schedule> scheduleList = scheduleRepository.findByWorkingDate(today);

        for (Schedule s : scheduleList) {
            Shift shift = s.getShift();
            Staff staff = s.getStaff();

            if (shift == null || staff == null || shift.getStartTime() == null || shift.getEndTime() == null)
                continue;

            LocalDateTime start = convertToLocalDateTime(shift.getStartTime());
            LocalDateTime end = convertToLocalDateTime(shift.getEndTime());
            LocalDateTime lastLogin = staff.getLastLogin() != null ? convertToLocalDateTime(staff.getLastLogin()) : null;

            boolean inCheck = now.isAfter(start.minusSeconds(1)) && now.isBefore(start.plusMinutes(6));
            boolean afterEnd = now.isAfter(end);

            if (inCheck && !staff.getOnShift()) {
                if (lastLogin != null) {
                    staff.setOnShift(true);

                    if (lastLogin.isAfter(start.plusMinutes(5))) {
                        staff.setMonthLateCount(staff.getMonthLateCount() +1);
                    }

                    staffRepository.save(staff);
                }
            }

            if (afterEnd && staff.getOnShift()) {
                staff.setOnShift(false);
                staffRepository.save(staff);

                if (!"Completed".equalsIgnoreCase(shift.getStatus())) {
                    shift.setStatus("Completed");
                    shiftRepository.save(shift);
                }
            }
        }
    }

    private LocalDateTime convertToLocalDateTime(Date date) {
        return date.toInstant().atZone(ZONE).toLocalDateTime();
    }

}


