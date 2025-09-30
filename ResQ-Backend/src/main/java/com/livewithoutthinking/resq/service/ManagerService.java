package com.livewithoutthinking.resq.service;

import com.livewithoutthinking.resq.dto.ScheduleDto;
import com.livewithoutthinking.resq.dto.StaffDto;
import com.livewithoutthinking.resq.dto.UserDto;
import com.livewithoutthinking.resq.entity.Shift;
import com.livewithoutthinking.resq.entity.Staff;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

public interface ManagerService {
    List<Staff> findOnlyStaff();
    List<Shift> findMineShift();
    List<Staff> findAllManager();

    List<StaffDto> findAllManagers();
    Optional<Staff> findManagerById(int staffId);
    List<StaffDto> searchManagers(String keyword);
    Staff createNew(UserDto dto, MultipartFile avatar);

    void updateShift(Integer id, ScheduleDto dto);
}
