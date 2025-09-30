package com.livewithoutthinking.resq.repository;

import com.livewithoutthinking.resq.dto.RequestRescueDto;
import com.livewithoutthinking.resq.dto.RescueDetailDTO;
import com.livewithoutthinking.resq.dto.RescueInfoDTO;
import com.livewithoutthinking.resq.entity.RequestRescue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface RequestRescueRepository extends JpaRepository<RequestRescue, Integer> {
    @Query("""
    SELECT 
        SUM(CASE WHEN r.rescueType = 'ResTow' THEN 1 ELSE 0 END),
        SUM(CASE WHEN r.rescueType = 'ResFix' THEN 1 ELSE 0 END),
        SUM(CASE WHEN r.rescueType = 'ResDrive' THEN 1 ELSE 0 END)
    FROM RequestRescue r
    WHERE DATE(r.createdAt) = :date
""")
    List<Object[]> countByTypeOnDate(@Param("date") LocalDate date);


    @Query("SELECT " +
            "SUM(CASE WHEN r.rescueType = 'ResTow' THEN 1 ELSE 0 END), " +
            "SUM(CASE WHEN r.rescueType = 'ResFix' THEN 1 ELSE 0 END), " +
            "SUM(CASE WHEN r.rescueType = 'ResDrive' THEN 1 ELSE 0 END) " +
            "FROM RequestRescue r " +
            "WHERE r.createdAt BETWEEN :start AND :end")
    List<Object[]> countByTypeInRange(@Param("start") LocalDateTime start,
                                      @Param("end") LocalDateTime end);

    @Query("""
    SELECT COUNT(r)
    FROM RequestRescue r
    WHERE FUNCTION('MONTH', r.createdAt) = FUNCTION('MONTH', CURRENT_DATE)
      AND FUNCTION('YEAR', r.createdAt) = FUNCTION('YEAR', CURRENT_DATE)
""")
    Long countTotalRescueInCurrentMonth();

    @Query(value = """
    SELECT COUNT(*)
    FROM requestrescue
    WHERE MONTH(created_at) = MONTH(CURRENT_DATE - INTERVAL 1 MONTH)
      AND YEAR(created_at) = YEAR(CURRENT_DATE - INTERVAL 1 MONTH)
""", nativeQuery = true)
    Long countTotalRescueInLastMonth();

    @Query("""
    SELECT COUNT(DISTINCT u.userId) 
    FROM User u 
    JOIN RequestRescue rThisMonth ON rThisMonth.user.userId = u.userId 
    WHERE rThisMonth.createdAt >= :startOfMonth
      AND EXISTS (
          SELECT 1 FROM RequestRescue rPrev
          WHERE rPrev.user.userId = u.userId
            AND rPrev.createdAt < :startOfMonth
      )
""")
    Long countReturningCustomers(@Param("startOfMonth") LocalDateTime startOfMonth);

    @Query("""
    SELECT COUNT(DISTINCT u.userId)
    FROM User u
    JOIN RequestRescue rLastMonth ON rLastMonth.user.userId = u.userId
    WHERE rLastMonth.createdAt >= :startOfLastMonth
      AND rLastMonth.createdAt < :startOfThisMonth
      AND EXISTS (
          SELECT 1 FROM RequestRescue rPrev
          WHERE rPrev.user.userId = u.userId
            AND rPrev.createdAt < :startOfLastMonth
      )
""")
    Long countReturningCustomersLastMonth(
            @Param("startOfLastMonth") LocalDateTime startOfLastMonth,
            @Param("startOfThisMonth") LocalDateTime startOfThisMonth
    );

    @Query("SELECT rr FROM RequestRescue rr WHERE rr.partner.partnerId = :partnerId ORDER BY rr.createdAt DESC")
    List<RequestRescue> searchByPartner(int partnerId);

    @Query("SELECT rr FROM RequestRescue rr WHERE rr.user.userId = :userId ORDER BY rr.createdAt DESC")
    List<RequestRescue> searchByUser(int userId);

    @Query("SELECT rr FROM RequestRescue rr WHERE rr.uLocation LIKE :keyword OR rr.user.fullName LIKE :keyword OR " +
            "rr.partner.user.fullName LIKE :keyword ORDER BY rr.createdAt DESC")
    List<RequestRescue>searchRR(String keyword);

    @Query("SELECT rr FROM RequestRescue rr WHERE (rr.uLocation LIKE :keyword OR rr.partner.user.fullName LIKE :keyword) AND " +
            "rr.user.userId = :userId ORDER BY rr.createdAt DESC")
    List<RequestRescue>searchRRWithUser(int userId, String keyword);

    @Query("SELECT rr FROM RequestRescue rr WHERE (rr.uLocation LIKE :keyword OR rr.user.fullName LIKE :keyword) AND " +
            "rr.partner.partnerId = :partnerId ORDER BY rr.createdAt DESC")
    List<RequestRescue>searchRRWithPartner(int partnerId, String keyword);

    @Query("SELECT count(r) FROM RequestRescue r WHERE r.user.userId = :userId")
    int countTotalRequestRescue(int userId);

    @Query("SELECT new com.livewithoutthinking.resq.dto.RequestRescueDto(" +
            "r.rrid, u.fullName, u.userId, p.user.fullName, p.partnerId) " +
            "FROM RequestRescue r " +
            "JOIN r.user u " +
            "JOIN r.partner p")
    List<RequestRescueDto> findAllRequestRescueDtos();

    @Query("SELECT new com.livewithoutthinking.resq.dto.RequestRescueDto(" +
            "r.rrid, u.fullName, u.userId, p.user.fullName, p.partnerId) " +
            "FROM RequestRescue r " +
            "JOIN r.user u " +
            "JOIN r.partner p " +
            "WHERE r.rrid = :rrid")
    RequestRescueDto findRequestRescueDtoByRrid(Integer rrid);

    Optional<RequestRescue> findRequestRescueByPartner_PartnerIdAndStatus(int partnerId,String status);

    // Tìm theo RRID (và status COMPLETED)
    List<RequestRescue> findByUser_UserIdAndStatusOrderByCreatedAtDesc(int userId, String status);


    // Tìm danh sách theo PartnerID (và status COMPLETED), sắp xếp mới nhất
    List<RequestRescue> findByPartner_PartnerIdAndStatusOrderByCreatedAtDesc(int partnerId, String status);


    @Query("SELECT rr FROM RequestRescue rr LEFT JOIN FETCH rr.bill WHERE rr.rrid = :rrid")
    Optional<RequestRescue> findWithBillById(@Param("rrid") Integer rrid);

    @Query("SELECT new com.livewithoutthinking.resq.dto.RescueInfoDTO( " +
            "b.requestRescue.rrid, " +
            "b.billId, " +
            "b.requestRescue.user.fullName, " +
            "b.requestRescue.partner.user.fullName, " +
            "b.payment.paymentId, " +
            "b.payment.method, " +
            "b.total, " +
            "b.totalPrice, " +
            "b.method, " +
            "b.createdAt, " +
            "b.status) " +
            "FROM Bill b")
    List<RescueInfoDTO> findAllRescueInfo();

    @Modifying
    @Query("UPDATE Bill b SET b.status = :status WHERE b.billId = :billId")
    void updateBillStatusByBillId(@Param("billId") int billId, @Param("status") String status);

    @Query("SELECT new com.livewithoutthinking.resq.dto.RescueDetailDTO( " +
            "b.requestRescue.user.fullName, " +
            "b.requestRescue.user.sdt, " +
            "b.requestRescue.partner.user.fullName, " +
            "b.requestRescue.partner.user.sdt, " +
            "b.requestRescue.rescueType, " +
            "b.requestRescue.startTime, " +
            "b.requestRescue.endTime, " +
            "b.appFee, " +
            "b.total, " +
            "b.method, " +
            "b.status, " +
            "b.requestRescue.status, " +
            "b.requestRescue.cancelNote) " +
            "FROM Bill b " +
            "WHERE b.requestRescue.rrid = :rrid")
    Optional<RescueDetailDTO> findRescueDetailByRRID(@Param("rrid") int rrid);

}
