package com.livewithoutthinking.resq.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfiguration implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry){
        registry
                .addMapping("/**")
                .allowedOrigins("*")
                .allowedHeaders("*")
                .allowedMethods("GET","POST", "PUT", "DELETE", "OPTIONS", "PATH", "HEAD", "PATCH");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Mọi URL bắt đầu bằng /uploads/ sẽ được map tới thư mục uploads trong ổ đĩa
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + System.getProperty("user.dir") + "/uploads/");
        // thư mục "uploads/" nằm cùng cấp target hoặc root project
        registry.addResourceHandler("/secure_uploads/**")
                .addResourceLocations("file:" + System.getProperty("user.dir") + "/secure_uploads/");
    }
}
