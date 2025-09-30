package com.livewithoutthinking.resq.service;

import com.livewithoutthinking.resq.dto.ScheduleDto;
import com.livewithoutthinking.resq.entity.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AdminService {
    // Refund
    List<RefundRequest> findAllRefundRequests();
    Optional<RefundRequest> findRefundById(Integer id);
    List<RefundRequest> findRefundByName(String name);
    RefundRequest saveRefundRequest(RefundRequest refunds);

    // Schedule
    List<Shift> findAllShift();
    Optional<Shift> findShiftById(Integer id);
    Shift saveShift(Shift shift);
    void updateShift(Integer id, ScheduleDto dto);
    void deleteShift(Integer id);

    List<Schedule> findAllSchedule();
    List<Schedule> findScheduleByShift(Shift shift);
    Optional<Schedule> findScheduleByShiftIdAndWorkingDate(int shiftId, LocalDate workingDate);
    void deleteSchedule(Integer id);
    Schedule saveSchedule(Schedule schedule);

    CancelShift saveCancelShift(CancelShift cancelShift);

}
