package com.livewithoutthinking.resq.repository;

import com.livewithoutthinking.resq.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;


@Repository
public interface ReportRepository extends JpaRepository<Report, Integer> {
    //    List<Report> findByCustomerNameContainingIgnoreCase(String customerName);
    List<Report> findByStatusIgnoreCase(String status);
    List<Report> findByStaff_StaffId(Integer staffId);
    @Query("SELECT r FROM Report r WHERE r.defendantPartner.partnerId = :partnerId ORDER BY r.createdAt DESC")
    List<Report> findByDefendantPartnerId(@Param("partnerId") Integer partnerId);

    List<Report> findByDefendantCustomerUserIdOrderByCreatedAtDesc(Integer userId);

    @Modifying
    @Transactional
    @Query("UPDATE Report r SET r.within24H = false WHERE r.within24H = true AND r.createdAt <= :expiredTime")
    void updateExpiredReports(@Param("expiredTime") Date expiredTime);

    @Query("SELECT rp FROM Report rp WHERE rp.requestRescue.rrid = :rrId")
    List<Report> findByRequestRescue(int rrId);
}
