package com.livewithoutthinking.resq.service.serviceImpl;

import com.livewithoutthinking.resq.dto.UserDashboard;
import com.livewithoutthinking.resq.dto.UserDto;
import com.livewithoutthinking.resq.entity.*;
import com.livewithoutthinking.resq.mapper.UserMapper;
import com.livewithoutthinking.resq.repository.*;
import com.livewithoutthinking.resq.service.CustomerService;
import com.livewithoutthinking.resq.service.UserRankService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.*;

@Service
public class CustomerServiceImpl implements CustomerService {
    @Autowired
    private UserRepository customerRepository;
    @Autowired
    private BillRepository billRepository;
    @Autowired
    private RequestRescueRepository requestRescueRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private NotificationRepository notificationRepository;
    @Autowired
    private NotificationTemplateRepository notificationTemplateRepository;
    @Autowired
    private PaymentRepository paymentRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserRankService userRankService;
    @Autowired
    private PasswordEncoder encoder;

    public List<UserDto> findAllCustomers() {
        List<User> result = customerRepository.findAllCustomers();
        List<UserDto> dtos = new ArrayList<>();
        for (User user : result) {
            UserDto dto = UserMapper.toDTO(user);
            List<RequestRescue> requestRescues = requestRescueRepository.searchByUser(user.getUserId());
            dto.setTotalRescues(requestRescues.size());
            dtos.add(dto);
        }
        return dtos;
    }

    public UserDto searchCustomerById(int customerId){
        User user = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("user not found"));
        UserDto dto = UserMapper.toDTO(user);
        return dto;
    }

    public Optional<User> findCustomerById(int userId) {
        return customerRepository.findById(userId);
    }

    public List<UserDto> searchCustomers(String keyword){
        List<User> result = customerRepository.searchCustomers("%"+keyword+"%");
        List<UserDto> userDtos = new ArrayList<>();
        for(User user : result){
            UserDto dto = UserMapper.toDTO(user);
            List<RequestRescue> requestRescues = requestRescueRepository.searchByUser(user.getUserId());
            dto.setTotalRescues(requestRescues.size());
            userDtos.add(dto);
        }
        return userDtos;
    }

    public UserDashboard customerDashboard(int userId){
        List<RequestRescue> userRR = requestRescueRepository.searchByUser(userId);
        List<Bill> userBill = billRepository.findBillsByUser(userId);
        UserDashboard userDash = new UserDashboard();
        int totalSuccess = 0;
        int totalCancel = 0;
        for(RequestRescue rr : userRR){
            if(rr.getStatus().equalsIgnoreCase("completed")){
                totalSuccess++;
            }else if(rr.getStatus().equalsIgnoreCase("canceled")){
                totalCancel++;
            }
        }
        double paid = 0.0;
        for(Bill b : userBill){
            paid = paid + b.getTotalPrice();
        }

        userDash.setTotalSuccess(totalSuccess);
        userDash.setTotalCancel(totalCancel);
        if(!userRR.isEmpty()){
            userDash.setPercentSuccess((double) totalSuccess /userRR.size()*100);
        }
        userDash.setTotalAmount(paid);
        return userDash;
    }

    public User createNew(UserDto dto, MultipartFile avatar){
        Role role = roleRepository.findByName("CUSTOMER");
        User newCus = UserMapper.toEntity(dto, encoder);
        newCus.setStatus("WAITING");
        newCus.setRole(role);
        newCus.setCreatedAt(new Date());
        if (avatar != null && !avatar.isEmpty()) {
            try {
                String fileName = UUID.randomUUID().toString() + "_" + avatar.getOriginalFilename();
                String uploadDir = System.getProperty("user.dir") + "/uploads/avatar/";
                File dir = new File(uploadDir);
                if (!dir.exists()) dir.mkdirs();

                File file = new File(uploadDir + fileName);
                avatar.transferTo(file);
                newCus.setAvatar(fileName);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        User savedCus = customerRepository.save(newCus);

        Payment cashPayment = new Payment();
        cashPayment.setName("CASH");
        cashPayment.setUser(newCus);
        cashPayment.setCreatedAt(new Date());
        paymentRepository.save(cashPayment);

        NotificationTemplate notiTemplate = notificationTemplateRepository.findByNotiType("NOTI");

        Notification noti = new Notification();
        noti.setNotificationTemplate(notiTemplate);
        noti.setUser(newCus);
        noti.setCreatedAt(new Date())   ;
        noti.setMessage("Welcome to ResQ! Your default password is $resQ2025. \n" +
                "We recommend changing it immediately to keep your account secure!");
        notificationRepository.save(noti);
        return savedCus;
    }

    public UserDto getCustomer(int customerId){
        User user = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if(user != null){
            UserDto userDto = UserMapper.toDTO(user);
            return userDto;
        }
        return null;
    }

    public UserDto updateCustomer(UserDto dto){
        User user = customerRepository.findById(dto.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        if(dto.getUsername() != null || dto.getUsername().isEmpty()){
            user.setUsername(dto.getUsername());
        }
        if(dto.getEmail() != null && !dto.getEmail().isEmpty()){
            user.setEmail(dto.getEmail());
        }
        if (dto.getDob() != null) {
            user.setDob(dto.getDob());
        }
        if (dto.getGender() != null && !dto.getGender().trim().isEmpty()) {
            user.setGender(dto.getGender());
        }
        customerRepository.save(user);
        return UserMapper.toDTO(user);
    }

    public void updateCustomerPoint(int rrId){
        RequestRescue requestRescue = requestRescueRepository.findById(rrId)
                .orElseThrow(() -> new RuntimeException("RequestRescue not found"));
        Bill bill = billRepository.findBillsByReqResQ(requestRescue.getRrid());
        User user = userRepository.findUserById(requestRescue.getUser().getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        int newPoint = (int) (bill.getTotal() / 1000);
        user.setLoyaltyPoint(newPoint+user.getLoyaltyPoint());

        userRepository.save(user);
        userRankService.updateUserRank(user.getUserId());
    }
}
