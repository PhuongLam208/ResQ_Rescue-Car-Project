package com.livewithoutthinking.resq.service.serviceImpl;

import com.livewithoutthinking.resq.dto.ScheduleDto;
import com.livewithoutthinking.resq.dto.StaffDto;
import com.livewithoutthinking.resq.dto.UserDto;
import com.livewithoutthinking.resq.entity.*;
import com.livewithoutthinking.resq.mapper.StaffMapper;
import com.livewithoutthinking.resq.mapper.UserMapper;
import com.livewithoutthinking.resq.repository.*;
import com.livewithoutthinking.resq.service.ManagerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

@Service
public class ManagerServiceImpl implements ManagerService {

    @Autowired
    private StaffRepository staffRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ShiftRepository shiftRepository;
    @Autowired
    private ScheduleRepository scheduleRepository;
    @Autowired
    private ConversationRepository conversationRepository;
    @Autowired
    private MessageRepository messageRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private PasswordEncoder encoder;

    @Override
    public List<Staff> findOnlyStaff() {
        return staffRepository.findStaffByRoleName("STAFF");
    }

    @Override
    public List<Staff> findAllManager() {
        return staffRepository.findStaffByRoleName("MANAGER");
    }

    @Override
    public List<Shift> findMineShift() {
        Staff mine = staffRepository.findByUser_Username(SecurityContextHolder.getContext().getAuthentication().getName());
        List<Schedule> mySchedules = scheduleRepository.findByManager(mine);
        List<Shift> shifts = new ArrayList<>();
        for (Schedule schedule : mySchedules) {
            int shiftId = schedule.getShift().getShiftId();
            Optional<Shift> optionalShift = shiftRepository.findById(shiftId);
            optionalShift.ifPresent(shifts::add);
        }
        return shifts;
    }

    @Override
    @Transactional
    public void updateShift(Integer id, ScheduleDto dto) {
        Shift shift = shiftRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Shift not found"));

        shift.setTitle(dto.getTitle());
        shift.setDescription(dto.getDescription());
        shift.setEventColor(dto.getEventColor());
        shift.setUpdatedAt(new Date());

        shiftRepository.save(shift);

        List<Schedule> existing = scheduleRepository.findByShift(shift);
        scheduleRepository.deleteAll(existing);

        List<Integer> staffIds = dto.getStaffIds();
        if (staffIds != null) {
            for (Integer staffId : staffIds) {
                Staff staff = staffRepository.findById(staffId)
                        .orElseThrow(() -> new RuntimeException("Staff ID " + staffId + " not found"));

                Schedule ss = new Schedule();
                ss.setShift(shift);
                ss.setStaff(staff);

                scheduleRepository.save(ss);
            }
        }
    }

    public List<StaffDto> findAllManagers() {
        Role roleManager = roleRepository.findByName("MANAGER");
        List<Staff> staffs = staffRepository.findAllStaffs(roleManager.getRoleId());
        List<StaffDto> staffDtos = new ArrayList<StaffDto>();
        for (Staff staff : staffs) {
            StaffDto dto = StaffMapper.toDTO(staff);
            staffDtos.add(dto);
        }
        return staffDtos;
    }

    public Optional<Staff> findManagerById(int staffId) {
        return staffRepository.findById(staffId);
    }

    public List<StaffDto> searchManagers(String keyword){
        Role roleManager = roleRepository.findByName("MANAGER");
        List<Staff> result = staffRepository.searchStaffs("%"+keyword+"%", roleManager.getRoleId());
        List<StaffDto> staffDtos = new ArrayList<>();
        for(Staff staff : result){
            StaffDto staffDto = StaffMapper.toDTO(staff);
            staffDtos.add(staffDto);
        }
        return staffDtos;
    }


    public Staff createNew(UserDto dto, MultipartFile avatar){
        User newUser = new User();
        Role role = roleRepository.findByName("MANAGER");
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

        Staff newManager = new Staff();
        newManager.setUser(newUser);
        newManager.setHiredDate(new Date());
        newManager.setAvgTime((float) 0.0);
        return staffRepository.save(newManager);
    }

}

