package com.livewithoutthinking.resq.repository;

import com.livewithoutthinking.resq.entity.Shift;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ShiftRepository extends JpaRepository<Shift, Integer> {
    Optional<Shift> findByShiftId(int shiftId);
    List<Shift> findAllByIsRecurringTrue();

}
