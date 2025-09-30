package com.livewithoutthinking.resq.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;
@Data
@AllArgsConstructor
public class DailyRenvenueData {

        private Date date;
        private double resFix;
        private double resDrive;
        private double resTow;


}
