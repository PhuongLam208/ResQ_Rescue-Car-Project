package com.livewithoutthinking.resq.mapper;
import com.livewithoutthinking.resq.dto.UserDto;
import com.livewithoutthinking.resq.entity.User;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.crypto.password.PasswordEncoder;


@Getter
@Setter
public class UserMapper {

    public static UserDto toDTO(User user) {
        if (user == null) return null;

        UserDto dto = new UserDto();
        dto.setUserId(user.getUserId());
        dto.setUsername(user.getUsername());
        dto.setFullName(user.getFullName());
        dto.setEmail(user.getEmail());
        dto.setSdt(user.getSdt());
        dto.setStatus(user.getStatus());
        dto.setDob(user.getDob());
        dto.setGender(user.getGender());
        dto.setAddress(user.getAddress());
        dto.setAvatar(user.getAvatar());
        dto.setPhoneVerified(user.isPhoneVerified());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        dto.setLanguage(user.getLanguage());
        dto.setAppColor(user.getAppColor());
        dto.setLoyaltyPoint(user.getLoyaltyPoint());
        dto.setRoleName(user.getRole() != null ? user.getRole().getRoleName() : null);
        dto.setRole(user.getRole() != null ? user.getRole().getRoleId() : 0);
        if(user.getPersonalData() != null){
            dto.setPdStatus(user.getPersonalData().getVerificationStatus());
        }
        return dto;
    }

    public static  User toEntity(UserDto dto, PasswordEncoder encoder) {
        if (dto == null) return null;
        User user = new User();
        user.setFullName(dto.getFullName());
        user.setEmail(dto.getEmail());
        user.setSdt(dto.getSdt());
        user.setAddress(dto.getAddress());
        user.setUsername(dto.getUsername());
        user.setPassword(encoder.encode(dto.getPassword()));
        return user;
    }
}

