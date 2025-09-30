package com.livewithoutthinking.resq.service;


import com.livewithoutthinking.resq.dto.ReportDto;
import com.livewithoutthinking.resq.dto.ReportResolveDto;
import com.livewithoutthinking.resq.entity.Notification;
import com.livewithoutthinking.resq.entity.Report;
import com.livewithoutthinking.resq.entity.Staff;
import com.livewithoutthinking.resq.entity.User;
import com.livewithoutthinking.resq.repository.NotificationRepository;
import com.livewithoutthinking.resq.repository.ReportRepository;
import com.livewithoutthinking.resq.repository.StaffRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
public class ReportService {

    @Autowired
    private ReportRepository reportRepository;
    @Autowired
    private StaffRepository staffRepository;
    @Autowired
    private NotificationService notificationService;

    public ReportService(ReportRepository reportRepository) {
        this.reportRepository = reportRepository;
    }

    public Report addNewReport(Report report) {
    return reportRepository.save(report);
    }

    public List<Report> showAllReport() {
        return reportRepository.findAll();
    }

    public Report getReportById(int id) {
        return reportRepository.findById(id).get();
    }

    public List<Report> findByStatusIgnoreCase(String status) {
        return reportRepository.findByStatusIgnoreCase(status);
    }

    public List<Report> findByStaff_id(Integer staffId) {
            return reportRepository.findByStaff_StaffId(staffId);
    }


    public List<Report> getReportsByPartnerId(Integer partnerId) {
        return reportRepository.findByDefendantPartnerId(partnerId);
    }
    public List<Report> getReportsByDefendantCustomer(Integer userId) {
        return reportRepository.findByDefendantCustomerUserIdOrderByCreatedAtDesc(userId);
    }

    public Report save(Report report) {
        return reportRepository.save(report);
    }

    @Scheduled(fixedRate = 60 * 60 * 1000)
    public void updateExpiredReports() {
        System.out.println("Checking expired reports...");
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.HOUR, -24);
        Date expiredTime = cal.getTime();

        reportRepository.updateExpiredReports(expiredTime);
        System.out.println("Expired reports updated!");
    }
    public ReportDto resolveReport(Integer reportId, ReportResolveDto dto) {
        // Tìm report theo id
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Report not found!"));

        // Xác định status để lưu vào report dựa trên dto.getStatus()
        String status = dto.getStatus() != null ? dto.getStatus().toLowerCase() : "";
        // Status để lưu vào report
        String ViewStatus;
        String title;      // Tiêu đề để gửi notification

        switch (status) {
            case "resolved":
                ViewStatus ="REPORT_RESOLVE";
                status = "REPORT_RESOLVE";
                title = "Your report has been resolved";
                break;
            case "rejected":
                ViewStatus ="REPORT_REJECT";
                status = "REPORT_REJECT";
                title = "Your report has been rejected";
                break;
            default:
                ViewStatus ="REPORT_PENDING";
                status = "REPORT_PENDING";
                title = "Update on your report";
                break;
        }

        // Cập nhật thông tin report
        report.setResponseToComplainant(dto.getResponseToComplainant());
        report.setStatus(status);           // Lưu status mới
        report.setUpdatedAt(new Date());
        report.setWithin24H(false);

        // Gán resolver nếu có
        if (dto.getResolverId() != null) {
            Staff resolver = staffRepository.findById(dto.getResolverId())
                    .orElseThrow(() -> new RuntimeException("Resolver (staff) not found"));
            report.setResolver(resolver);
        }

        Report saved = reportRepository.save(report);

        // Xác định complainant user (có thể là customer hoặc partner)
        User complainantUser = null;
        if (report.getComplainantCustomer() != null) {
            complainantUser = report.getComplainantCustomer();
        } else if (report.getComplainantPartner() != null) {
            complainantUser = report.getComplainantPartner().getUser();
        }

        // Nếu có complainantUser và có nội dung phản hồi thì gửi notification
        if (complainantUser != null && dto.getResponseToComplainant() != null && !dto.getResponseToComplainant().isEmpty()) {
            notificationService.notifyUser(
                    complainantUser.getUserId(),
                    title,
                    dto.getResponseToComplainant(),
                    ViewStatus
            );
        }

        return new ReportDto(saved);
    }
}
