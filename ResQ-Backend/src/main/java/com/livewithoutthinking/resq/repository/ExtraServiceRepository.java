package com.livewithoutthinking.resq.repository;

import com.livewithoutthinking.resq.entity.ExtraService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ExtraServiceRepository extends JpaRepository<ExtraService, Integer> {
    @Query("SELECT e FROM ExtraService e WHERE e.requestRescue.rrid = :rrId")
    List<ExtraService> findExtrSrvByRR(int rrId);
}
