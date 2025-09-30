package com.livewithoutthinking.resq.service.serviceImpl;

import com.livewithoutthinking.resq.dto.PaymentDto;
import com.livewithoutthinking.resq.entity.Payment;
import com.livewithoutthinking.resq.entity.RefundRequest;
import com.livewithoutthinking.resq.entity.User;
import com.livewithoutthinking.resq.repository.PaymentRepository;
import com.livewithoutthinking.resq.repository.RefundRepository;
import com.livewithoutthinking.resq.repository.UserRepository;
import com.livewithoutthinking.resq.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class PaymentServiceImpl implements PaymentService {
    @Autowired
    private PaymentRepository paymentRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RefundRepository refundRepository;

    public List<PaymentDto> customerPayments(int customerId){
        List<Payment> result = paymentRepository.customerPayments(customerId);
        List<PaymentDto> dtos = new ArrayList<PaymentDto>();
        for(Payment payment : result){
            PaymentDto dto = new PaymentDto();
            dto.setPaymentId(Math.toIntExact(payment.getPaymentId()));
            dto.setName(payment.getName());
            dtos.add(dto);
        }
        return dtos;
    }

    public List<Payment> getCustomerPayments() {

        return paymentRepository.findPaymentsByCustomers();
    }

    public List<Payment> getCustomerPaymentsByCustomerId(Integer customerId) {
        return paymentRepository.findByUser_UserId(customerId);
    }

    public List<Payment> getCustomerPaymentsByPartnerId(@Param("partnerId") Integer partnerId) {
        return paymentRepository.findPaymentsByPartnerId(partnerId);
    }
    public boolean getRefundPayment(int refundId) {
        RefundRequest refundRequest = refundRepository.findById(refundId)
                .orElseThrow(() -> new RuntimeException("refund not found"));
        Payment userPayment = paymentRepository.partnerPaypalPayment(refundRequest.getUser().getUserId());
        if (userPayment != null) {
            return true;
        }
        return false;
    }
    public List<Payment> getPaymentsByPartnerId(Integer partnerId) {
        return paymentRepository.findPaymentsByPartnerId(partnerId);
    }

    public List<PaymentDto> appPayments(Integer userId) {
        List<Payment> result = paymentRepository.customerPayments(userId);
        List<PaymentDto> dtos = new ArrayList<>();
        for (Payment payment : result) {
            PaymentDto dto = new PaymentDto();
            dto.setPaymentId(Math.toIntExact(payment.getPaymentId()));
            dto.setName(payment.getName());
            dto.setPaypalEmail(payment.getPaypalEmail());
            dto.setMethod(payment.getMethod());
            dto.setCreatedAt(payment.getCreatedAt());
            dtos.add(dto);
        }
        return dtos;
    }

    public Payment createPayment(int userId, PaymentDto paymentDto) {
        User user = userRepository.findUserById(userId)
                .orElseThrow(() -> new RuntimeException("user not found"));

        Payment payment = new Payment();
        payment.setName(paymentDto.getName());
        payment.setPaypalEmail(paymentDto.getPaypalEmail());
        payment.setMethod(paymentDto.getMethod());
        payment.setUser(user);
        payment.setCreatedAt(new Date());
        return paymentRepository.save(payment);
    }

    public Payment updatePayment(PaymentDto paymentDto) {
        Payment payment = paymentRepository.findById(paymentDto.getPaymentId())
                .orElseThrow(() -> new RuntimeException("payment not found"));
        payment.setPaypalEmail(paymentDto.getPaypalEmail());
        payment.setName(paymentDto.getName());
        payment.setUpdatedAt(new Date());
        return paymentRepository.save(payment);
    }

    public void deletePayment(int paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("payment not found"));
        System.out.println("ID: " + paymentId);
        System.out.println("Payment: " + payment);
        paymentRepository.delete(payment);
    }

    public Payment getPartnerPaypalPayment(int partnerId){
        return paymentRepository.partnerPaypalPayment(partnerId);
    }

}
