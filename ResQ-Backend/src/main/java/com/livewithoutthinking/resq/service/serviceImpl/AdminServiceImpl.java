package com.livewithoutthinking.resq.service.serviceImpl;

import com.livewithoutthinking.resq.dto.ScheduleDto;
import com.livewithoutthinking.resq.entity.*;
import com.livewithoutthinking.resq.repository.*;
import com.livewithoutthinking.resq.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Optional;


@Service
public class AdminServiceImpl implements AdminService {
    @Autowired
    private RefundRepository refundRepository;
    @Autowired
    private StaffRepository staffRepository;
    @Autowired
    private ShiftRepository shiftRepository;
    @Autowired
    private ScheduleRepository scheduleRepository;
    @Autowired
    private CancelShiftRepository cancelShiftRepository;

    private Date parseDate(String str) {
        if (str == null || str.isBlank()) return null;
        try {
            return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm").parse(str);
        } catch (ParseException e) {
            throw new RuntimeException("Invalid date format: " + str);
        }
    }

    @Override
    public List<RefundRequest> findAllRefundRequests() {
        return refundRepository.findAll();
    }

    @Override
    public Optional<RefundRequest> findRefundById(Integer id) {
        return refundRepository.findById(id);
    }

    @Override
    public List<RefundRequest> findRefundByName(String name) {
        return refundRepository.findByName("%"+name.toLowerCase()+"%");
    }

    @Override
    public RefundRequest saveRefundRequest(RefundRequest refunds) {
        return refundRepository.save(refunds);
    }

    @Override
    public List<Shift> findAllShift() {
        return shiftRepository.findAll();
    }

    @Override
    public Optional<Shift> findShiftById(Integer id) {
        return shiftRepository.findById(id);
    }

    @Override
    public Shift saveShift(Shift shift) {
        return shiftRepository.save(shift);
    }

    @Override
    @Transactional
    public void updateShift(Integer id, ScheduleDto dto) {
        Shift shift = shiftRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Shift not found"));

        shift.setTitle(dto.getTitle());
        shift.setDescription(dto.getDescription());
        shift.setStartTime(parseDate(dto.getStartTime()));
        shift.setEndTime(parseDate(dto.getEndTime()));
        shift.setEventColor(dto.getEventColor());
        shift.setStatus(dto.getStatus());
        shift.setIsRecurring(dto.getIsRecurring());
        shift.setRecurrenceType(dto.getRecurrenceType());
        shift.setRecurrenceInterval(dto.getRecurrenceInterval());
        shift.setRecurrenceDays(dto.getRecurrenceDays());
        shift.setRecurrenceEndDate(parseDate(dto.getRecurrenceEndDate()));
        shift.setUpdatedAt(new Date());

        shiftRepository.save(shift);

        List<Schedule> existing = scheduleRepository.findByShift(shift);
        scheduleRepository.deleteAll(existing);

        Staff manager = staffRepository.findById(dto.getManagerId())
                .orElseThrow(() -> new RuntimeException("Manager ID " + dto.getManagerId() + " not found"));

        List<Integer> staffIds = dto.getStaffIds();
        if (staffIds != null) {
            for (Integer staffId : staffIds) {
                Staff staff = staffRepository.findById(staffId)
                        .orElseThrow(() -> new RuntimeException("Staff ID " + staffId + " not found"));

                Schedule ss = new Schedule();
                ss.setShift(shift);
                ss.setStaff(staff);
                ss.setManager(manager);

                scheduleRepository.save(ss);
            }
        }
    }

    @Override
    @Transactional
    public void deleteShift(Integer id) {
        Shift shift = shiftRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Schedule not found"));

        List<Schedule> linked = scheduleRepository.findByShift(shift);
        scheduleRepository.deleteAll(linked);
        // xóa theo staff nên cũng xóa hết manager rồi

        shiftRepository.delete(shift);
    }

    @Override
    public List<Schedule> findAllSchedule() {
        return scheduleRepository.findAll();
    }

    @Override
    public List<Schedule> findScheduleByShift(Shift shift) {
        return scheduleRepository.findByShift(shift);
    }

    @Override
    public Optional<Schedule> findScheduleByShiftIdAndWorkingDate(int shiftId, LocalDate workingDate) {
        Optional<Shift> shift = shiftRepository.findById(shiftId);
        if (shift.isPresent()) {
            List<Schedule> ScheList = scheduleRepository.findByShift(shift.get());
            for (Schedule s : ScheList) {
                if (workingDate.equals(s.getWorkingDate())) {
                    return Optional.of(s);
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public void deleteSchedule(Integer id) {
        scheduleRepository.deleteById(id);
    }

    @Override
    public Schedule saveSchedule(Schedule schedule) {
        return scheduleRepository.save(schedule);
    }

    @Override
    public CancelShift saveCancelShift(CancelShift cancelShift) {
        return cancelShiftRepository.save(cancelShift);
    }
}
