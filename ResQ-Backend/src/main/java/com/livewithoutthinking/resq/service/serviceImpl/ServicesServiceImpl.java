package com.livewithoutthinking.resq.service.serviceImpl;

import com.livewithoutthinking.resq.dto.ServiceDto;
import com.livewithoutthinking.resq.entity.Services;
import com.livewithoutthinking.resq.mapper.ServiceMapper;
import com.livewithoutthinking.resq.repository.ServiceRepository;
import com.livewithoutthinking.resq.service.ServicesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ServicesServiceImpl implements ServicesService {
    @Autowired
    private ServiceRepository serviceRepository;

    public List<ServiceDto> findByServiceType(String keyword) {
        List<Services> results = serviceRepository.findByServiceType(keyword);
        List<ServiceDto> dtos = new ArrayList<>();
        for (Services service : results) {
            ServiceDto dto = ServiceMapper.toDTO(service);
            dtos.add(dto);
        }
        return dtos;
    }

    public List<Services> findAll() {
        return serviceRepository.findAll();
    }

    public Services findById( int id) {
        return serviceRepository.findById(id).orElse(null);
    }

    public Services save(Services service) {
        return serviceRepository.save(service);
    }

    public Services updatePrices(int serviceId, double fixedPrice, double pricePerKm) {
        Optional<Services> optionalService = serviceRepository.findById(serviceId);
        if (optionalService.isPresent()) {
            Services service = optionalService.get();
            service.setFixedPrice(fixedPrice);
            service.setPricePerKm(pricePerKm);
            return serviceRepository.save(service);
        }
        return null;
    }

    public List<Services> searchByName(String keyword) {
        return serviceRepository.findByServiceNameContainingIgnoreCase(keyword);
    }

    public List<Services> filterByType(String serviceType) {
        return serviceRepository.findByServiceTypeIgnoreCase(serviceType);
    }
}
