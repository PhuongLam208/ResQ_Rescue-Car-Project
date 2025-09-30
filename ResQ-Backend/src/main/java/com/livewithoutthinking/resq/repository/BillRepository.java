package com.livewithoutthinking.resq.repository;

import com.livewithoutthinking.resq.dto.MonthlyRevenueDto;
import com.livewithoutthinking.resq.entity.Bill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface BillRepository extends JpaRepository<Bill, Integer> {
    @Query(value = """
    SELECT 
        DATE(rr.created_at) AS date,
        SUM(CASE WHEN rr.rescue_type = 'ResFix' THEN b.total_price ELSE 0 END) AS resFix,
        SUM(CASE WHEN rr.rescue_type = 'ResDrive' THEN b.total_price ELSE 0 END) AS resDrive,
        SUM(CASE WHEN rr.rescue_type = 'ResTow' THEN b.total_price ELSE 0 END) AS resTow
    FROM bill b
    JOIN requestrescue rr ON b.RRID = rr.RRID
    WHERE DATE(rr.created_at) BETWEEN :startDate AND :endDate
    GROUP BY DATE(rr.created_at)
    ORDER BY DATE(rr.created_at)
""", nativeQuery = true)
    List<Object[]> getRevenueByDateRange(LocalDate startDate, LocalDate endDate);

    @Query("""
    SELECT SUM(b.total)
    FROM Bill b
    WHERE FUNCTION('MONTH', b.createdAt) = FUNCTION('MONTH', CURRENT_DATE)
      AND FUNCTION('YEAR', b.createdAt) = FUNCTION('YEAR', CURRENT_DATE)
""")
    Double getTotalRevenueInCurrentMonth();


    @Query(value = """
    SELECT SUM(total_price)
    FROM bill
    WHERE MONTH(created_at) = MONTH(CURRENT_DATE - INTERVAL 1 MONTH)
      AND YEAR(created_at) = YEAR(CURRENT_DATE - INTERVAL 1 MONTH)
""", nativeQuery = true)
    Double getTotalRevenueInLastMonth();


    @Query("SELECT NEW com.livewithoutthinking.resq.dto.MonthlyRevenueDto(" +
            "MONTH(b.createdAt), YEAR(b.createdAt), SUM(b.total), SUM(b.appFee), MAX(b.status), MAX(b.createdAt)) " +
            "FROM Bill b " +
            "WHERE YEAR(b.createdAt) = :year AND b.requestRescue.partner.partnerId = :partnerId " +
            "GROUP BY YEAR(b.createdAt), MONTH(b.createdAt) " +
            "ORDER BY MONTH(b.createdAt)")
    List<MonthlyRevenueDto> getMonthlyRevenueByPartner(@Param("partnerId") int partnerId,
                                                       @Param("year") int year);


    @Query("SELECT NEW com.livewithoutthinking.resq.dto.MonthlyRevenueDto(" +
            "MONTH(b.createdAt), YEAR(b.createdAt), SUM(b.total), SUM(b.appFee), MAX(b.status), MAX(b.createdAt)) " +
            "FROM Bill b " +
            "WHERE b.requestRescue.partner.partnerId = :partnerId " +
            "GROUP BY YEAR(b.createdAt), MONTH(b.createdAt) " +
            "ORDER BY YEAR(b.createdAt) DESC, MONTH(b.createdAt) DESC")
    List<MonthlyRevenueDto> getAllMonthlyRevenueByPartner(@Param("partnerId") int partnerId);

    @Query("SELECT b FROM Bill b WHERE b.requestRescue.rrid = :rrId AND b.status != 'REFUNDED' ")
    Bill findBillsByReqResQ(int rrId);
    @Query("SELECT b FROM Bill b WHERE b.requestRescue.partner.partnerId = :partnerId")
    List<Bill> findBillsByPartner(int partnerId);
    @Query("SELECT b FROM Bill b WHERE b.requestRescue.user.userId = :userId")
    List<Bill> findBillsByUser(int userId);

    @Query("SELECT b FROM Bill b " +
            "WHERE b.requestRescue.user.userId = :userId " +
            "AND b.requestRescue.status = :rescueStatus " +
            "AND b.status = :paymentStatus")
    List<Bill> findBillsByUserAndStatus(@Param("userId") Integer userId,
                                        @Param("rescueStatus") String rescueStatus,
                                        @Param("paymentStatus") String paymentStatus);

    @Query("SELECT b FROM Bill b " +
            "JOIN b.requestRescue r " +
            "WHERE r.user.userId = :userId " +
            "AND r.status = 'COMPLETED' " +
            "AND b.status = 'SUCCESS'")
    List<Bill> findSuccessfulBillsByUserId(@Param("userId") int userId);

}
