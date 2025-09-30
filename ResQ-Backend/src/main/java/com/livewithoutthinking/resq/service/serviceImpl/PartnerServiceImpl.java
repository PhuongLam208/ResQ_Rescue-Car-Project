package com.livewithoutthinking.resq.service.serviceImpl;

import com.livewithoutthinking.resq.dto.LocationRequest;
import com.livewithoutthinking.resq.dto.NearbyPartnerDTO;
import com.livewithoutthinking.resq.dto.PartnerDto;
import com.livewithoutthinking.resq.dto.UserDashboard;
import com.livewithoutthinking.resq.entity.*;
import com.livewithoutthinking.resq.mapper.PartnerMapper;
import com.livewithoutthinking.resq.repository.*;
import com.livewithoutthinking.resq.service.PartnerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.util.*;
import java.time.LocalDateTime;

@Service
public class PartnerServiceImpl implements PartnerService {
    @Autowired
    private PartnerRepository partnerRepo;
    @Autowired
    private RequestRescueRepository requestResQRepo;
    @Autowired
    private BillRepository billRepo;
    @Autowired
    private NotificationTemplateRepository notiTemplateRepo;
    @Autowired
    private NotificationRepository notiRepo;
    @Autowired
    private DocumentaryRepository documentaryRepo;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private UserRepository userRepository;

    public List<Partner> findAll(){
        return partnerRepo.findAll();
    }

    public Partner findById(Integer id) {
        return partnerRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Partner not found"));
    }

    public void updatePartnerStatus(Integer partnerId, String status, LocalDateTime blockUntil) {
        Partner partner = partnerRepo.findById(partnerId)
                .orElseThrow(() -> new RuntimeException("Partner not found"));
        partner.setStatus(status);
        partner.setBlockUntil(blockUntil);
        partnerRepo.save(partner);
    }

    public Partner findByUser(int userId) {
        return partnerRepo.findByUser(userId);
    }

    public List<Report> searchByUserUsername(@Param("username") String username){
        List<Partner> partners = partnerRepo.searchByUserUsername(username);
        List<Report> allReports = new ArrayList<>();

        for (Partner partner : partners) {
            if (partner.getReports() != null) {
                allReports.addAll(partner.getReports());
            }
        }

        return allReports;
    }

    public List<Partner> searchByUsernameContainingIgnoreCase(String keyword) {
        return partnerRepo.findByUser_UsernameContainingIgnoreCase(keyword);
    }

    public List<PartnerDto> findAllDto(){
        List<Partner> partnerList =  partnerRepo.findAll();
        List<PartnerDto> showList = new ArrayList<>();
        for (Partner partner : partnerList) {
            PartnerDto dto = PartnerMapper.toDTO(partner);
            showList.add(dto);
        }
        return showList;
    }
    public Optional<PartnerDto> findPartnerById(int partnerId) {
        Partner result = partnerRepo.findPartnerById(partnerId);
        PartnerDto dto = PartnerMapper.toDTO(result);
        return Optional.ofNullable(dto);
    }
    public List<PartnerDto> searchPartners(String keyword){
        List<Partner> result = partnerRepo.searchPartners("%"+keyword+"%");
        List<PartnerDto> showList = new ArrayList<>();
        for (Partner partner : result) {
            PartnerDto dto = PartnerMapper.toDTO(partner);
            showList.add(dto);
        }
        return showList;
    }
    public UserDashboard partnerDashboard(int partnerId){
        List<RequestRescue> partnerRR = requestResQRepo.searchByPartner(partnerId);
        List<Bill> partnerBill = billRepo.findBillsByPartner(partnerId);
        UserDashboard partnerDash = new UserDashboard();
        int totalSuccess = 0;
        int totalCancel = 0;
        for(RequestRescue rr : partnerRR){
            if(rr.getStatus().equalsIgnoreCase("completed")){
                totalSuccess++;
            }else if(rr.getStatus().equalsIgnoreCase("canceled")){
                totalCancel++;
            }
        }
        double revenue = 0.0;
        for(Bill b : partnerBill){
            revenue = revenue + b.getTotalPrice();
        }

        partnerDash.setTotalSuccess(totalSuccess);
        partnerDash.setTotalCancel(totalCancel);
        if(!partnerRR.isEmpty()){
            partnerDash.setPercentSuccess((double)totalSuccess/partnerRR.size()*100);
        }
        partnerDash.setTotalAmount(revenue);
        return partnerDash;
    }
    public boolean approvePartner(int partnerId){
        boolean isNew = false;
        Partner partner = partnerRepo.findPartnerById(partnerId);
        if(!partner.isVerificationStatus()){
            partner.setVerificationStatus(true);
            isNew = true;
        }
        if(partner.getResTow() == 2){
            partner.setResTow(1);
        }
        if(partner.getResDrive() == 2){
            partner.setResDrive(1);
        }
        if(partner.getResFix() == 2){
            partner.setResFix(1);
        }
        Partner savedPartner = partnerRepo.save(partner);
        User user = partner.getUser();
        if(user != null && !user.getRole().getRoleName().equalsIgnoreCase("partner")){
            Role role = roleRepository.findByName("PARTNER");
            user.setRole(role);
            userRepository.save(user);
        }
        List<Documentary> unverifiedDocs = documentaryRepo.getUnverifiedPartnerDoc(partnerId);
        for(Documentary doc : unverifiedDocs){
            doc.setDocumentStatus("APPROVED");
            documentaryRepo.save(doc);
        }
        if(savedPartner != null){
            Notification notification = new Notification();
            NotificationTemplate notiTemplate = notiTemplateRepo.findByNotiType("DOCUMENT_APPROVE");
            notification.setNotificationTemplate(notiTemplate);
            notification.setUser(partner.getUser());
            if(isNew){
                notification.setMessage("We have successfully verified your documents. Now you have become our partner.");
            }else{
                notification.setMessage("Your request for new service has been verified.");
            }
            notification.setCreatedAt(new Date());
            notiRepo.save(notification);
        }
        return true;
    }

    @Override
    public Partner save(Partner partner) {
        return partnerRepo.save(partner);
    }

    public List<NearbyPartnerDTO> findNearbyPartners(LocationRequest request) {
        List<Partner> partners = partnerRepo.findAll();

        List<NearbyPartnerDTO> nearby = new ArrayList<>();

        for (Partner partner : partners) {
            double distance = haversine(request.getLat(), request.getLng(),
                    partner.getLatitude(), partner.getLongitude());

            if (distance <= request.getRadiusKm()) {
                nearby.add(new NearbyPartnerDTO(
                        partner.getPartnerId(),
                        partner.getUser().getFullName(),
                        partner.getLatitude(),
                        partner.getLongitude(),
                        Math.round(distance * 100.0) / 100.0
                ));
            }
        }

        // Sắp xếp tăng dần theo khoảng cách
        nearby.sort(Comparator.comparingDouble(NearbyPartnerDTO::getDistanceKm));
        return nearby;
    }

    // Hàm Haversine
    private double haversine(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // bán kính Trái Đất (km)
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    public boolean updatePartnerWalletAmount(int partnerId){
        Partner partner = partnerRepo.findPartnerById(partnerId);
        partner.setWalletAmount(BigDecimal.valueOf(50000));
        partnerRepo.save(partner);
        return true;
    }
}
