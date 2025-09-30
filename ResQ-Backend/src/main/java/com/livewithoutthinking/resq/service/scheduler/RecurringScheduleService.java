package com.livewithoutthinking.resq.service.scheduler;

import com.livewithoutthinking.resq.entity.Schedule;
import com.livewithoutthinking.resq.entity.Shift;
import com.livewithoutthinking.resq.repository.CancelShiftRepository;
import com.livewithoutthinking.resq.repository.ScheduleRepository;
import com.livewithoutthinking.resq.repository.ShiftRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecurringScheduleService {
    private final ShiftRepository shiftRepository;
    private final ScheduleRepository scheduleRepository;
    private final CancelShiftRepository cancelShiftRepository;

    @Transactional
    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Ho_Chi_Minh")
    public void generateRecurringSchedulesForToday() {
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"));

        List<Shift> shifts = shiftRepository.findAllByIsRecurringTrue();

        for (Shift shift : shifts) {
            if (shouldGenerateToday(shift, today)) {
                boolean exists = scheduleRepository.existsByShiftAndWorkingDate(shift, today);
                boolean isCanceled = cancelShiftRepository.existsByShiftAndCanceledDate(shift, today);
                if (!exists && !isCanceled) {
                    // Clone các schedule mẫu từ ngày bắt đầu shift (đã set sẵn)
                    LocalDate originalDate = shift.getStartTime()
                            .toInstant()
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate();

                    List<Schedule> baseSchedules = scheduleRepository.findByShiftAndWorkingDate(shift, originalDate);

                    List<Schedule> newSchedules = baseSchedules.stream().map(old -> {
                        Schedule s = new Schedule();
                        s.setShift(old.getShift());
                        s.setManager(old.getManager());
                        s.setStaff(old.getStaff());
                        s.setWorkingDate(today);
                        return s;
                    }).collect(Collectors.toList());

                    scheduleRepository.saveAll(newSchedules);
                }
            }
        }
    }

    public boolean shouldGenerateToday(Shift shift, LocalDate today) {
        if (!Boolean.TRUE.equals(shift.getIsRecurring())) return false;

        LocalDate endDate = shift.getRecurrenceEndDate() != null
                ? shift.getRecurrenceEndDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
                : null;
        if (endDate != null && today.isAfter(endDate)) return false;

        String type = shift.getRecurrenceType();
        int interval = Optional.ofNullable(shift.getRecurrenceInterval()).orElse(1);
        LocalDate startDate = shift.getStartTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        switch (type) {
            case "DAILY":
                long daysBetween = ChronoUnit.DAYS.between(startDate, today);
                return daysBetween % interval == 0;

            case "WEEKLY":
                long weeksBetween = ChronoUnit.WEEKS.between(startDate, today);
                DayOfWeek todayDay = today.getDayOfWeek();
                String[] recurrenceDays = Optional.ofNullable(shift.getRecurrenceDays()).orElse("").split(",");
                boolean matchDay = Arrays.stream(recurrenceDays)
                        .map(String::trim)
                        .anyMatch(d -> todayDay.name().equalsIgnoreCase(d));
                return weeksBetween % interval == 0 && matchDay;

            case "MONTHLY":
                long monthsBetween = ChronoUnit.MONTHS.between(
                        YearMonth.from(startDate),
                        YearMonth.from(today)
                );
                return monthsBetween % interval == 0
                        && today.getDayOfMonth() == startDate.getDayOfMonth();

            default:
                return false;
        }
    }

}
