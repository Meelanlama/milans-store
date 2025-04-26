package com.milan.config;

import com.milan.dto.ReviewDto;
import com.milan.model.Review;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ProjectConfig {

    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }

}
