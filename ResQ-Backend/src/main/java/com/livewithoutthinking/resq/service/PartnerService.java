package com.livewithoutthinking.resq.service;

import com.livewithoutthinking.resq.dto.LocationRequest;
import com.livewithoutthinking.resq.dto.NearbyPartnerDTO;
import com.livewithoutthinking.resq.dto.PartnerDto;
import com.livewithoutthinking.resq.dto.UserDashboard;
import com.livewithoutthinking.resq.entity.Partner;

import java.util.List;
import java.util.Optional;

public interface PartnerService {
    List<Partner> findAll();
    Partner findById(Integer id);
    Partner findByUser(int uId);
    List<Partner> searchByUsernameContainingIgnoreCase(String keyword);
    Partner save(Partner partner);

    List<PartnerDto> findAllDto();
    List<PartnerDto> searchPartners(String keyword);
    UserDashboard partnerDashboard(int partnerId);
    Optional<PartnerDto> findPartnerById(int partnerId);
    boolean approvePartner(int partnerId);

    boolean updatePartnerWalletAmount(int partnerId);
    List<NearbyPartnerDTO> findNearbyPartners(LocationRequest request);

}
