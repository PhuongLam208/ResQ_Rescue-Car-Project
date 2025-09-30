package com.livewithoutthinking.resq.service;


import com.livewithoutthinking.resq.dto.PaymentDto;
import com.livewithoutthinking.resq.entity.Payment;
import com.livewithoutthinking.resq.repository.PaymentRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;

import java.util.List;

public interface PaymentService {
    List<PaymentDto> customerPayments(int customerId);
    List<Payment> getCustomerPayments();
    List<Payment> getCustomerPaymentsByCustomerId(Integer customerId);
    List<Payment> getCustomerPaymentsByPartnerId(@Param("partnerId") Integer partnerId);
    List<Payment> getPaymentsByPartnerId(Integer partnerId);

    List<PaymentDto> appPayments(Integer userId);
    Payment createPayment(int userId, PaymentDto paymentDto);
    Payment updatePayment(PaymentDto paymentDto);
    void deletePayment(int paymentId);
    Payment getPartnerPaypalPayment(int partnerId);
    boolean getRefundPayment(int refundId);
}
