package com.livewithoutthinking.resq.repository;

import com.livewithoutthinking.resq.entity.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface FeedbackRepository extends JpaRepository<Feedback, Integer> {
    @Query("SELECT f FROM Feedback f WHERE f.requestRescue.partner.partnerId = :partnerId" )
    List<Feedback> searchByPartner(int partnerId);
    @Query("SELECT f FROM Feedback f WHERE f.requestRescue.rrid = :rrId")
    List<Feedback> searchByRR(int rrId);
}
