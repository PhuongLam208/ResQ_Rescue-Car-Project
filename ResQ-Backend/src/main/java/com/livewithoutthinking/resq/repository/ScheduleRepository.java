package com.livewithoutthinking.resq.repository;

import com.livewithoutthinking.resq.entity.Schedule;
import com.livewithoutthinking.resq.entity.Shift;
import com.livewithoutthinking.resq.entity.Staff;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ScheduleRepository extends JpaRepository<Schedule, Integer> {
    List<Schedule> findByShift(Shift shift);
    List<Schedule> findByManager(Staff staff);
    List<Schedule> findByStaff(Staff staff);
    List<Schedule> findByWorkingDate(LocalDate workingDate);
    boolean existsByShiftAndWorkingDate(Shift shift, LocalDate today);
    List<Schedule> findByShiftAndWorkingDate(Shift shift, LocalDate workingDate);
}
