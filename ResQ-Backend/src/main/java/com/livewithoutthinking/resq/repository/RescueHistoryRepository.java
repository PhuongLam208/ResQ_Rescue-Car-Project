package com.livewithoutthinking.resq.repository;

import com.livewithoutthinking.resq.dto.RescueDetailDTO;
import com.livewithoutthinking.resq.dto.RescueInfoDTO;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.livewithoutthinking.resq.entity.RescueHistory;

import java.util.List;
import java.util.Optional;

@Repository
public interface RescueHistoryRepository extends CrudRepository<RescueHistory, Integer> {

//    @Query("SELECT new com.livewithoutthinking.resq.dto.RescueInfoDTO( " +
//            "r.rrid, " +
//            "r.bill.billId, " +
//            "r.user.fullName, " +
//            "r.partner.user.fullName, " +
//            "r.bill.payment.paymentId, " +
//            "r.bill.payment.method, " +
//            "r.bill.total, " +
//            "r.bill.totalPrice, " +
//            "r.bill.method, " +
//            "r.bill.createdAt, " +
//            "r.bill.status) " +
//            "FROM RequestRescue r")
//    List<RescueInfoDTO> findAllRescueInfo();
//
//    @Modifying
//    @Query("UPDATE Bill b SET b.status = :status WHERE b.billId = :billId")
//    void updateBillStatusByBillId(@Param("billId") int billId, @Param("status") String status);
//
//    @Query("SELECT new com.livewithoutthinking.resq.dto.RescueDetailDTO( " +
//            "r.user.fullName, " +
//            "r.user.sdt, " +
//            "r.partner.user.fullName, " +
//            "r.partner.user.sdt, " +
//            "r.rescueType, " +
//            "r.createdAt, " +
//            "r.endTime, " +
//            "r.bill.appFee, " +
//            "r.bill.total, " +
//            "r.bill.method, " +
//            "r.bill.status, " +
//            "r.status, " +
//            "r.cancelNote) " +
//            "FROM RequestRescue r " +
//            "WHERE r.rrid = :rrid")
//    Optional<RescueDetailDTO> findRescueDetailByRRID(@Param("rrid") int rrid);

}
