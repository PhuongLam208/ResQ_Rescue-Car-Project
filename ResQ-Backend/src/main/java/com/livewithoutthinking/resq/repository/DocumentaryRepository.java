package com.livewithoutthinking.resq.repository;

import com.livewithoutthinking.resq.entity.Documentary;
import com.livewithoutthinking.resq.entity.Partner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface DocumentaryRepository extends JpaRepository<Documentary, Integer> {
    List<Documentary> findByPartner_PartnerId(Integer partnerId);

    @Query("SELECT doc FROM Documentary doc WHERE doc.documentStatus = 'PENDING' AND doc.partner.partnerId = :partnerId")
    List<Documentary> getUnverifiedPartnerDoc(int partnerId);

    @Query("SELECT doc FROM Documentary doc WHERE doc.documentType = :type AND doc.partner.partnerId = :partnerId")
    Documentary getDocumentaryByType(String type, int partnerId);

    List<Documentary> findByPartner(Partner partner);

    List<Documentary> findByUser_UserId(Integer userId);

    @Query("SELECT doc FROM Documentary doc WHERE doc.documentType LIKE :plateNo")
    List<Documentary> getDocumentaryByPlateNo(String plateNo);
}
