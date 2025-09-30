package com.livewithoutthinking.resq.repository;

import com.livewithoutthinking.resq.entity.RequestService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface RequestServiceRepository extends JpaRepository<RequestService, Integer> {
    @Query("SELECT rs FROM RequestService rs WHERE rs.request.rrid = :rrId")
    List<RequestService> findByRequest(int rrId);

    @Query("SELECT rs FROM RequestService rs WHERE rs.request.rrid = :rrId")
    List<RequestService> getReqSrvByResquest(int rrId);
}
