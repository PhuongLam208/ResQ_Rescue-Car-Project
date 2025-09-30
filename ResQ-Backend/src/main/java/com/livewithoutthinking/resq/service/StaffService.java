package com.livewithoutthinking.resq.service;

import com.livewithoutthinking.resq.dto.StaffDto;
import com.livewithoutthinking.resq.dto.UserDto;
import com.livewithoutthinking.resq.entity.RefundRequest;
import com.livewithoutthinking.resq.entity.RequestRescue;
import com.livewithoutthinking.resq.entity.Shift;
import com.livewithoutthinking.resq.entity.Staff;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

public interface StaffService {
    List<Staff> findAllStaff();
    Optional<Staff> findById(Integer id);
    RefundRequest saveRR(RefundRequest refunds);
    Optional<RequestRescue> findRRById(Integer id);
    Staff findByUser_Username(String username);
    Staff save(Staff staff);
    Optional<Staff> findByUser_UserId(Integer userId);

    List<Shift> findMineShift();
    List<StaffDto> findAllStaffs();
    Optional<Staff> findStaffById(int staffId);
    List<StaffDto> searchStaffs(String keyword);
    Staff createNewStaff(UserDto dto, MultipartFile avatar);
    List<Staff> findByOnShiftTrueAndIsOnlineTrue();
}
