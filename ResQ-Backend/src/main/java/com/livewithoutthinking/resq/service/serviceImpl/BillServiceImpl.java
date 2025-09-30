package com.livewithoutthinking.resq.service.serviceImpl;

import com.livewithoutthinking.resq.dto.MonthlyRevenueDto;
import com.livewithoutthinking.resq.entity.Bill;
import com.livewithoutthinking.resq.repository.BillRepository;
import com.livewithoutthinking.resq.service.BillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BillServiceImpl implements BillService {

    @Autowired
    private BillRepository billRepository;

    public Bill findBillsByReqResQ(int rrId) {
        return billRepository.findBillsByReqResQ(rrId);
    }

    public List<Bill> findBillsByPartner(int partnerId) {
        return billRepository.findBillsByPartner(partnerId);
    }

    public List<MonthlyRevenueDto> getMonthlyRevenueByPartner(int partnerId, int year) {
        return billRepository.getMonthlyRevenueByPartner(partnerId, year);
    }

    public List<MonthlyRevenueDto> getAllMonthlyRevenueByPartner(int partnerId) {
        return billRepository.getAllMonthlyRevenueByPartner(partnerId);
    }
    public List<Bill> findBillsByUserAndStatus(Integer userId, String rescueStatus, String paymentStatus) {
        return billRepository.findBillsByUserAndStatus(userId, rescueStatus, paymentStatus);
    }

    @Override
    public Bill save(Bill bill) {
        return billRepository.save(bill);
    }
}
