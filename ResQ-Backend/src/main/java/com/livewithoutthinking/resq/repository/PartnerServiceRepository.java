package com.livewithoutthinking.resq.repository;

import com.livewithoutthinking.resq.entity.Partner;
import com.livewithoutthinking.resq.entity.PartnerService;
import com.livewithoutthinking.resq.entity.Services;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PartnerServiceRepository extends JpaRepository<PartnerService, Integer> {
    // có thể thêm query để lấy dịch vụ của partner nếu cần
    boolean existsByPartnerAndServices(Partner partner, Services services);
    @Query("SELECT ps FROM PartnerService ps WHERE ps.partner = :partner AND ps.services.serviceType = :type")
    List<PartnerService> findByPartnerAndServiceType(@Param("partner") Partner partner, @Param("type") String type);

}
