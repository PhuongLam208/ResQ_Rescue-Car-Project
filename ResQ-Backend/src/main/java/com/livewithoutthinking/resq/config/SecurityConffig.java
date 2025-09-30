package com.livewithoutthinking.resq.config;

// Đã phân quyền cơ bản của 3 role, tính năng nào dùng chung 3 role thì ghép lại chia sau

import com.livewithoutthinking.resq.filter.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConffig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration)
            throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean // Đã phân quyền cơ bản của 3 role, tính năng nào dùng chung 3 role thì ghép lại chia sau
    SecurityFilterChain api(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(au -> au
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                                .requestMatchers("/api/resq/**","/api/resq/verify/**").permitAll()
                                .requestMatchers("/api/resq/admin/**").hasRole("ADMIN")
                                .requestMatchers("/api/resq/manager/**").hasRole("MANAGER")
                                .requestMatchers("/api/resq/staff/**").hasRole("STAFF")
                                .requestMatchers("/api/resq/customer/**", "/api/resq/pcrescue/**").hasAnyRole("USER", "PARTNER")
                                .requestMatchers("/api/resq/partner/**", "/api/resq/partner-chat/**").hasRole("PARTNER")
                                .requestMatchers("/api/resq/notification-templates/**", "/api/notifications/**", "/api/messages/**", "/api/chatbot/**").permitAll()
                                .requestMatchers("/api/paypal/**", "/api/paypal/webhook/**").permitAll()
                                .requestMatchers("/ws/**").permitAll()
                                .requestMatchers("/uploads/**", "/uploads/avatar/**", "/secure_uploads/**").permitAll()
                        .anyRequest().authenticated())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .cors(config -> new CorsConfiguration().applyPermitDefaultValues());
        return http.build();
    }

}
