package com.livewithoutthinking.resq.service;

import com.livewithoutthinking.resq.dto.MonthlyRevenueDto;
import com.livewithoutthinking.resq.entity.Bill;

import java.util.List;

public interface BillService {
    Bill findBillsByReqResQ(int rrId);
    List<Bill> findBillsByPartner(int parnerId);
    List<MonthlyRevenueDto> getMonthlyRevenueByPartner(int partnerId, int year);
    List<MonthlyRevenueDto> getAllMonthlyRevenueByPartner(int partnerId);
    List<Bill> findBillsByUserAndStatus(Integer userId, String rescueStatus, String paymentStatus);
    Bill save(Bill bill);
}
