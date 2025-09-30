package com.livewithoutthinking.resq.repository;

import com.livewithoutthinking.resq.entity.CancelShift;
import com.livewithoutthinking.resq.entity.Shift;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface CancelShiftRepository extends JpaRepository<CancelShift, Integer> {
    boolean existsByShiftAndCanceledDate(Shift shift, LocalDate canceledDate);
    boolean existsByShift_ShiftIdAndCanceledDate(int shiftId, LocalDate canceledDate);
    List<CancelShift> findByCanceledDate(LocalDate date);
}
