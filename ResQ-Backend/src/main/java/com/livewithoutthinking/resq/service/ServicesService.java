package com.livewithoutthinking.resq.service;

import com.livewithoutthinking.resq.dto.ServiceDto;
import com.livewithoutthinking.resq.entity.Services;
import com.livewithoutthinking.resq.repository.ServiceRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

public interface ServicesService {
    List<ServiceDto> findByServiceType(String keyword);
    List<Services> findAll();
    Services findById( int id);
    Services save(Services service);
    Services updatePrices(int serviceId, double fixedPrice, double pricePerKm);
    List<Services> searchByName(String keyword);
    List<Services> filterByType(String serviceType);

}
