package com.livewithoutthinking.resq.service;

import com.livewithoutthinking.resq.dto.DailyRenvenueData;
import com.livewithoutthinking.resq.repository.BillRepository;
import com.livewithoutthinking.resq.repository.RequestRescueRepository;
import com.livewithoutthinking.resq.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DashboardService {
    private final RequestRescueRepository requestRescueRepository;
    private final BillRepository billRepository;
    private final UserRepository userRepository;

    public DashboardService(RequestRescueRepository requestRescueRepository, BillRepository billRepository, UserRepository userRepository) {
        this.requestRescueRepository = requestRescueRepository;
        this.billRepository = billRepository;
        this.userRepository = userRepository;
    }

//    public List<Map<String, Object>> getRescueChartData(LocalDate start, LocalDate end) {
//        // Nếu start hoặc end null => tính tuần hiện tại
//        if (start == null || end == null) {
//            LocalDate today = LocalDate.now();
//
//            // Lấy ngày đầu tuần (thứ 2)
//            start = today.with(java.time.DayOfWeek.MONDAY);
//
//            // Lấy ngày cuối tuần (chủ nhật)
//            end = start.plusDays(6);
//        }
//
//        LocalDateTime startTime = start.atStartOfDay();
//        LocalDateTime endTime = end.atTime(LocalTime.MAX); // 23:59:59.999999999
//
//        List<Object[]> result = requestRescueRepository.getRescueRevenueData(startTime, endTime);
//        List<Map<String, Object>> chartData = new ArrayList<>();
//
//        for (Object[] row : result) {
//            Map<String, Object> item = new HashMap<>();
//            item.put("date", row[0].toString());
//            item.put("ResTow", row[1]);
//            item.put("ResFix", row[2]);
//            item.put("ResDrive", row[3]);
//            chartData.add(item);
//        }
//
//        return chartData;
//    }
    public List<DailyRenvenueData> getRevenueByDateRange(LocalDate startDate, LocalDate endDate) {
        List<Object[]> rawData = billRepository.getRevenueByDateRange(startDate, endDate);
        return rawData.stream()
                .map(row -> new DailyRenvenueData(
                        (Date) row[0],
                        row[1] != null ? ((Number) row[1]).doubleValue() : 0,
                        row[2] != null ? ((Number) row[2]).doubleValue() : 0,
                        row[3] != null ? ((Number) row[3]).doubleValue() : 0
                ))
                .collect(Collectors.toList());
    }
    public Map<String, Long> getDailyRescueStats(LocalDate date) {
        List<Object[]> resultList = requestRescueRepository.countByTypeOnDate(date);
        Object[] result = resultList.isEmpty() ? new Object[]{0, 0, 0} : resultList.get(0);
        return mapResult(result);
    }

    public Map<String, Long> getRescueStatsInRange(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);

        List<Object[]> resultList = requestRescueRepository.countByTypeInRange(start, end);
        Object[] result = resultList.isEmpty() ? new Object[]{0, 0, 0} : resultList.get(0);
        return mapResult(result);
    }


    private Map<String, Long> mapResult(Object[] row) {
        Map<String, Long> map = new HashMap<>();
        map.put("towing", row[0] != null ? ((Number) row[0]).longValue() : 0);
        map.put("repairOnSite", row[1] != null ? ((Number) row[1]).longValue() : 0);
        map.put("driverReplacement", row[2] != null ? ((Number) row[2]).longValue() : 0);
        return map;
    }


    public Double getTotalRevenue() {
        return billRepository.getTotalRevenueInCurrentMonth() != null ? billRepository.getTotalRevenueInCurrentMonth() : 0.0;
    }

    public Long getTotalRescueThisMonth() {
        return requestRescueRepository.countTotalRescueInCurrentMonth();
    }

    public Long getTotalRescueLastMonth() {
        return requestRescueRepository.countTotalRescueInLastMonth();
    }
    public Long countNewCustomersThisMonth() {
        return userRepository.countNewCustomersThisMonth();
    }
    public Long countNewCustomersLastMonth() {
        LocalDate lastMonth = LocalDate.now().minusMonths(1);
        int month = lastMonth.getMonthValue();
        int year = lastMonth.getYear();

        return userRepository.countNewCustomersInMonth(month, year);

    }
    public Long getReturningCustomerCountThisMonth() {
        LocalDateTime startOfMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        return requestRescueRepository.countReturningCustomers(startOfMonth);
    }
    public Long getReturningCustomerCountLastMonth() {
        LocalDate firstDayOfThisMonth = LocalDate.now().withDayOfMonth(1);
        LocalDate firstDayOfLastMonth = firstDayOfThisMonth.minusMonths(1);

        LocalDateTime startOfThisMonth = firstDayOfThisMonth.atStartOfDay();
        LocalDateTime startOfLastMonth = firstDayOfLastMonth.atStartOfDay();

        return requestRescueRepository.countReturningCustomersLastMonth(startOfLastMonth, startOfThisMonth);
    }

    public Double getLastMonthRevenue() {
        Double total = billRepository.getTotalRevenueInLastMonth();
        return total != null ? total : 0.0;
    }


}
