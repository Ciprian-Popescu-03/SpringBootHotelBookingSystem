package com.hotelbooking.platform.mappers;

import com.hotelbooking.platform.dto.request.FeatureRequestDto;
import com.hotelbooking.platform.dto.response.FeatureResponseDto;
import com.hotelbooking.platform.entities.Feature;
import org.springframework.stereotype.Component;

@Component
public class FeatureMapper {

    public FeatureResponseDto toResponseDto(Feature feature) {
        return new FeatureResponseDto(
                feature.getId(),
                feature.getName()
        );
    }

    public Feature toEntity(FeatureRequestDto dto) {
        Feature feature = new Feature();
        feature.setName(dto.name());
        return feature;
    }
}
