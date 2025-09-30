package com.livewithoutthinking.resq.dto;

import com.livewithoutthinking.resq.entity.Report;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
public class ReportDto {

    @NotBlank(message = "Report name must not be blank")
    @Size(max = 255, message = "Report name must be at most 255 characters")
    private String name;

    @Size(max = 1000, message = "Description must be at most 1000 characters")
    private String description;

    @NotNull(message = "Rescue request ID is required")
    private Integer requestRescueId;

    @NotNull(message = "Reporter (staff) ID is required")
    private Integer staffId;
    private String staffName;

    private Integer resolverId;
    private String resolverName;

    // Complainant (CUSTOMER or PARTNER)
    @NotBlank(message = "Complainant type is required (CUSTOMER or PARTNER)")
    private String complainantType;

    @NotNull(message = "Complainant ID is required")
    private Integer complainantId;
    private String complainantName; // thêm tên complainant

    // Defendant (CUSTOMER or PARTNER)
    @NotBlank(message = "Defendant type is required (CUSTOMER or PARTNER)")
    private String defendantType;

    @NotNull(message = "Defendant ID is required")
    private Integer defendantId;
    private String defendantName; // thêm tên defendant

    @Size(max = 1000, message = "Response to complainant must be at most 1000 characters")
    private String responseToComplainant;

    //    @NotBlank(message = "Status is required (e.g. PENDING, RESOLVED, REJECTED)")
    @NotBlank(message = "Request is required")
    @Size(max = 1000, message = "Request must be at most 1000 characters")
    private String request;

    private boolean within24H;
    private Date createdAt;
    private MultipartFile pdfFile;

    private String status;

    public ReportDto(Report report) {
        this.name = report.getName();
        this.description = report.getDescription();
        this.requestRescueId = report.getRequestRescue().getUser().getUserId();

        this.staffId = report.getStaff().getStaffId();
        this.staffName = report.getStaff().getUser().getFullName();

        if (report.getResolver() != null) {
            this.resolverId = report.getResolver().getStaffId();
            this.resolverName = report.getResolver().getUser().getFullName();
        }

        if (report.getComplainantCustomer() != null) {
            this.complainantType = "CUSTOMER";
            this.complainantId = report.getComplainantCustomer().getUserId();
            this.complainantName = report.getComplainantCustomer().getFullName();
        } else if (report.getComplainantPartner() != null) {
            this.complainantType = "PARTNER";
            this.complainantId = report.getComplainantPartner().getPartnerId();
            this.complainantName = report.getComplainantPartner().getUser().getFullName();
        }

        if (report.getDefendantCustomer() != null) {
            this.defendantType = "CUSTOMER";
            this.defendantId = report.getDefendantCustomer().getUserId();
            this.defendantName = report.getDefendantCustomer().getFullName();
        } else if (report.getDefendantPartner() != null) {
            this.defendantType = "PARTNER";
            this.defendantId = report.getDefendantPartner().getPartnerId();
            this.defendantName = report.getDefendantPartner().getUser().getFullName();
        }

        this.responseToComplainant = report.getResponseToComplainant();
        this.status = report.getStatus();
        this.request = report.getRequest();
        this.within24H = report.isWithin24H();
        this.createdAt = report.getCreatedAt();
    }
}
