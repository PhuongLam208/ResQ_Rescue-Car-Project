package com.livewithoutthinking.resq.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

// UserSimpleDto.java
@Data
@Getter
@Setter
public class UserSearchDto {
    private Integer userId;
    private String fullName;
    private String username;

    public UserSearchDto(Integer userId, String fullName, String username) {
        this.userId = userId;
        this.fullName = fullName;
        this.username = username;
    }
}
