package com.livewithoutthinking.resq.repository;

import com.livewithoutthinking.resq.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface PaymentRepository extends JpaRepository<Payment, Integer> {
    @Query("""
    SELECT p FROM Payment p
    WHERE p.user.userId NOT IN (
        SELECT pt.user.userId FROM Partner pt
        UNION
        SELECT st.user.userId FROM Staff st
    )
""")
    List<Payment> findPaymentsByCustomers();
    @Query("""
    SELECT p FROM Payment p
    WHERE p.user.partner.partnerId = :partnerId
""")
    List<Payment> findPaymentsByPartnerId(@Param("partnerId") Integer partnerId);

    List<Payment> findByUser_UserId(Integer userId);

    @Query("SELECT p FROM Payment  p WHERE p.user.userId = :customerId")
    List<Payment> customerPayments(int customerId);

    @Query("SELECT p FROM Payment p WHERE p.user.userId = :userId AND p.name = :name")
    Payment customerPaymentId(int userId, String name);

    @Query("SELECT p FROM Payment p WHERE p.user.userId = :userId AND p.method = 'PAYPAl'")
    Payment partnerPaypalPayment(int userId);


}
