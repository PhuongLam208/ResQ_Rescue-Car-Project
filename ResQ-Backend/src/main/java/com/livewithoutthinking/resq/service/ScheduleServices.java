package com.livewithoutthinking.resq.service;

import com.livewithoutthinking.resq.dto.ScheduleDto;
import com.livewithoutthinking.resq.entity.Schedule;
import com.livewithoutthinking.resq.entity.Shift;
import com.livewithoutthinking.resq.repository.CancelShiftRepository;
import com.livewithoutthinking.resq.repository.ScheduleRepository;
import com.livewithoutthinking.resq.repository.ShiftRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ScheduleServices {
    @Autowired
    private ShiftRepository shiftRepository;
    @Autowired
    private ScheduleRepository scheduleRepository;
    @Autowired
    private CancelShiftRepository cancelShiftRepository;

    private final String DATE_PATTERN = "yyyy-MM-dd'T'HH:mm";
    private final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public List<ScheduleDto> reloadSchedule(List<Shift> shiftList) {
        Date now = new Date();
        List<ScheduleDto> response = new ArrayList<>();
        for (Shift s : shiftList) {
            if (!Boolean.TRUE.equals(s.getIsRecurring())) {
                // Chỉ cập nhật status nếu không phải lịch lặp
                s.setStatus(updateShiftStatus(s, now));
            }
            ScheduleDto dto = toDto(s);
            if (Boolean.TRUE.equals(s.getIsRecurring())) {
                // Lịch lặp: tạo danh sách bản sao ứng với mỗi ngày
                response.addAll(expandRecurringSchedule(dto));
            } else {
                // Lịch đơn: add như cũ
                response.add(dto);
            }
        }
        return response;
    }

    public List<ScheduleDto> expandRecurringSchedule(ScheduleDto dto) {

        List<ScheduleDto> occurrences = new ArrayList<>();
        // Parse ngày giờ bắt đầu & kết thúc từ DTO
        LocalDateTime originalStart = LocalDateTime.parse(dto.getStartTime());
        LocalDateTime originalEnd = LocalDateTime.parse(dto.getEndTime());

        // Thời điểm kết thúc chu kỳ lặp
        LocalDate recurrenceEndDate = (dto.getRecurrenceEndDate() == null || dto.getRecurrenceEndDate().isBlank())
                ? originalStart.toLocalDate().plusYears(1) // fallback nếu không có
                : LocalDateTime.parse(dto.getRecurrenceEndDate(), FORMATTER).toLocalDate();

        ChronoUnit unit;
        switch (dto.getRecurrenceType()) {
            case "DAILY" -> unit = ChronoUnit.DAYS;
            case "WEEKLY" -> unit = ChronoUnit.WEEKS;
            case "MONTHLY" -> unit = ChronoUnit.MONTHS;
            default -> {
                occurrences.add(dto);
                return occurrences;
            }
        }

        Duration duration = Duration.between(originalStart, originalEnd);
        int interval = Math.max(1, dto.getRecurrenceInterval()); // tránh chia 0

        for (LocalDateTime currentStart = originalStart;
             !currentStart.toLocalDate().isAfter(recurrenceEndDate);
             currentStart = currentStart.plus(interval, unit)) {

            // Nếu WEEKLY: kiểm tra ngày trong tuần có khớp không
            if ("WEEKLY".equals(dto.getRecurrenceType())) {
                DayOfWeek currentDay = currentStart.getDayOfWeek();
                List<String> allowedDays = Arrays.stream(
                                dto.getRecurrenceDays() == null ? new String[0] : dto.getRecurrenceDays().split(","))
                        .map(String::trim)
                        .map(String::toUpperCase)
                        .toList();

                if (allowedDays.stream().noneMatch(day -> day.equals(currentDay.name()))) {
                    continue;
                }
            }
            LocalDate today = LocalDate.now();

            // ✅ BỎ QUA nếu ngày này đã bị huỷ
            if (!currentStart.toLocalDate().isBefore(today) &&
                    cancelShiftRepository.existsByShift_ShiftIdAndCanceledDate(dto.getShiftId(), currentStart.toLocalDate())) {
                continue;
            }

            ScheduleDto occurrence = new ScheduleDto();
            BeanUtils.copyProperties(dto, occurrence);

            LocalDateTime currentEnd = currentStart.plus(duration);

            occurrence.setStartTime(currentStart.format(FORMATTER));
            occurrence.setEndTime(currentEnd.format(FORMATTER));

            // Cập nhật trạng thái
            LocalDateTime now = LocalDateTime.now();
            if (now.isBefore(currentStart)) {
                occurrence.setStatus("PENDING");
            } else if (now.isAfter(currentEnd)) {
                occurrence.setStatus("COMPLETED");
            } else {
                occurrence.setStatus("ON SHIFT");
            }

            occurrences.add(occurrence);
        }

        return occurrences;
    }

    private String updateShiftStatus(Shift s, Date now) {
        String newStatus;
        if (now.before(s.getStartTime())) {
            newStatus = "PENDING";
        } else if (now.after(s.getEndTime())) {
            newStatus = "COMPLETED";
        } else {
            newStatus = "ON SHIFT";
        }

        if (!newStatus.equals(s.getStatus())) {
            s.setStatus(newStatus);
            s.setUpdatedAt(now);
            shiftRepository.save(s);
        }
        return newStatus;
    }

    private ScheduleDto toDto(Shift s) {
        ScheduleDto dto = new ScheduleDto();
        dto.setShiftId(s.getShiftId());
        dto.setTitle(s.getTitle());
        dto.setDescription(s.getDescription());
        dto.setStatus(s.getStatus());
        dto.setEventColor(s.getEventColor());
        dto.setCreatorName(s.getCreator().getUser().getFullName());
        dto.setStartTime(dateToString(s.getStartTime()));
        dto.setEndTime(dateToString(s.getEndTime()));
        dto.setRecurrenceDays(s.getRecurrenceDays());
        dto.setIsRecurring(s.getIsRecurring());
        dto.setRecurrenceType(s.getRecurrenceType());
        dto.setRecurrenceInterval(s.getRecurrenceInterval());
        dto.setRecurrenceEndDate(dateToString(s.getRecurrenceEndDate()));

        List<Schedule> schedules = scheduleRepository.findByShift(s);
        dto.setStaffIds(schedules.stream().map(sch -> sch.getStaff().getStaffId()).collect(Collectors.toList()));
        if (!schedules.isEmpty() && schedules.get(0).getManager() != null) {
            dto.setManagerId(schedules.get(0).getManager().getStaffId());
        } else {
            throw new IllegalArgumentException("Manager not found");
        }
        return dto;
    }

    public String dateToString(Date date) {
        if (date == null) return "";
        return new SimpleDateFormat(DATE_PATTERN).format(date);
    }

    public Date stringToDate(String str) {
        if (str == null || str.trim().isEmpty()) return null;
        try {
            return new SimpleDateFormat(DATE_PATTERN).parse(str);
        } catch (ParseException e) {
            try {
                // fallback: only date
                return new SimpleDateFormat("yyyy-MM-dd").parse(str);
            } catch (ParseException e2) {
                throw new IllegalArgumentException("Invalid date format: " + str);
            }
        }
    }

    public LocalDateTime convertToLocalDateTime(Date date) {
        return date == null ? null : date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    public LocalDate convertToLocalDate(Date date) {
        return date == null ? null : date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

}


