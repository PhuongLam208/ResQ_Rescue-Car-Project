package com.livewithoutthinking.resq.service.serviceImpl;

import com.livewithoutthinking.resq.entity.ExtraService;
import com.livewithoutthinking.resq.repository.ExtraServiceRepository;
import com.livewithoutthinking.resq.service.ExtraServiceSrv;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.StringJoiner;

@Service
public class ExtraServiceSrvImpl implements ExtraServiceSrv {
    @Autowired
    private ExtraServiceRepository extraSrvRepo;

    public ExtraService findExtraSrvByReqResQ(int rrId){
         List<ExtraService> rrExtra =  extraSrvRepo.findExtrSrvByRR(rrId);
         ExtraService showExtraSrv = new ExtraService();
        StringJoiner reasonJoiner = new StringJoiner(", ");
        double totalExtra = 0.0;
        for (ExtraService extra : rrExtra) {
            totalExtra += extra.getPrice();
            reasonJoiner.add(extra.getReason());
        }
        showExtraSrv.setPrice(totalExtra);
        showExtraSrv.setReason(reasonJoiner.toString());
         return showExtraSrv;
    }
}
