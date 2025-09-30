package com.livewithoutthinking.resq.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SearchResultDto {
    private Integer id;
    private String name;
    private String username;
}
