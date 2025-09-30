package com.livewithoutthinking.resq.service.serviceImpl;

import com.livewithoutthinking.resq.dto.StaffDto;
import com.livewithoutthinking.resq.dto.UserDto;
import com.livewithoutthinking.resq.entity.*;
import com.livewithoutthinking.resq.mapper.StaffMapper;
import com.livewithoutthinking.resq.mapper.UserMapper;
import com.livewithoutthinking.resq.repository.*;
import com.livewithoutthinking.resq.service.StaffService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.*;

@Service
public class StaffServiceImpl implements StaffService {

    @Autowired
    private StaffRepository staffRepository;
    @Autowired
    private RequestRescueRepository requestRescueRepository;
    @Autowired
    private ShiftRepository shiftRepository;
    @Autowired
    private ScheduleRepository scheduleRepository;
    @Autowired
    private RefundRepository refundRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    RoleRepository roleRepository;
    @Autowired
    private PasswordEncoder encoder;

    @Override
    public List<Staff> findAllStaff() {
        return staffRepository.findAll();
    }

    @Override
    public Optional<Staff> findById(Integer id) {
        return staffRepository.findById(id);
    }

    @Override
    public RefundRequest saveRR(RefundRequest refunds) {
        return refundRepository.save(refunds);
    }

    @Override
    public Optional<RequestRescue> findRRById(Integer id) {
        return requestRescueRepository.findById(id)    ;
    }

    @Override
    public Staff findByUser_Username(String username) {
        return staffRepository.findByUser_Username(username);
    }

    @Override
    public Staff save(Staff staff) {
        return staffRepository.save(staff);
    }

    @Override
    public Optional<Staff> findByUser_UserId(Integer userId) {
        return staffRepository.findByUser_UserId(userId);
    }

    @Override
    public List<Shift> findMineShift() {
        Staff mine = staffRepository.findByUser_Username(SecurityContextHolder.getContext().getAuthentication().getName());
        List<Schedule> mySchedules = scheduleRepository.findByStaff(mine);
        List<Shift> shifts = new ArrayList<>();
        for (Schedule schedule : mySchedules) {
            int shiftId = schedule.getShift().getShiftId();
            Optional<Shift> optionalShift = shiftRepository.findById(shiftId);
            optionalShift.ifPresent(shifts::add);
        }
        return shifts;
    }

    public List<StaffDto> findAllStaffs() {
        Role roleStaff = roleRepository.findByName("STAFF");
        List<Staff> staffs = staffRepository.findAllStaffs(roleStaff.getRoleId());
        List<StaffDto> staffDtos = new ArrayList<StaffDto>();
        for (Staff staff : staffs) {
            StaffDto staffDto = StaffMapper.toDTO(staff);
            staffDtos.add(staffDto);
        }
        return staffDtos;
    }

    public Optional<Staff> findStaffById(int staffId) {
        return staffRepository.findById(staffId);
    }

    public List<StaffDto> searchStaffs(String keyword){
        Role roleStaff = roleRepository.findByName("STAFF");
        List<Staff> result = staffRepository.searchStaffs("%"+keyword+"%", roleStaff.getRoleId());
        List<StaffDto> staffDtos = new ArrayList<>();
        for(Staff staff : result){
            StaffDto staffDto = StaffMapper.toDTO(staff);
            staffDtos.add(staffDto);
        }
        return staffDtos;
    }
    public Staff createNewStaff(UserDto dto, MultipartFile avatar){
        User newUser = new User();
        Role role = roleRepository.findByName("STAFF");
        newUser = UserMapper.toEntity(dto, encoder);
        newUser.setRole(role);
        newUser.setCreatedAt(new Date());
        newUser.setStatus("ACTIVE");
        if (avatar != null && !avatar.isEmpty()) {
            try {
                String fileName = UUID.randomUUID().toString() + "_" + avatar.getOriginalFilename();
                String uploadDir = System.getProperty("user.dir") + "/uploads/avatar/";
                File dir = new File(uploadDir);
                if (!dir.exists()) dir.mkdirs();

                File file = new File(uploadDir + fileName);
                avatar.transferTo(file);
                newUser.setAvatar(fileName);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        userRepository.save(newUser);

        Staff newStaff = new Staff();
        newStaff.setUser(newUser);
        newStaff.setHiredDate(new Date());
        newStaff.setAvgTime((float) 0.0);
        return staffRepository.save(newStaff);
    }

    @Override
    public List<Staff> findByOnShiftTrueAndIsOnlineTrue() {
        return staffRepository.findByOnShiftTrueAndIsOnlineTrue();
    }


}
