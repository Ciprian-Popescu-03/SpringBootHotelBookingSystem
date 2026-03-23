package com.hotelbooking.platform.config;

import com.hotelbooking.platform.entities.Feature;
import com.hotelbooking.platform.entities.Room;
import com.hotelbooking.platform.repositories.FeatureRepository;
import com.hotelbooking.platform.repositories.RoomRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.List;
import java.util.Set;

@Configuration
@Profile({"dev", "test"})
public class RoomConfig {

    @Bean
    CommandLineRunner commandLineRunner(
            RoomRepository roomRepository,
            FeatureRepository featureRepository) {
        return args -> {
            if (roomRepository.count() > 0 || featureRepository.count() > 0) {
                return;
            }

            Feature wifi = new Feature("WiFi");
            Feature balcony = new Feature("Balcony");
            Feature ac = new Feature("Air Conditioning");
            Feature oceanView = new Feature("Ocean View");
            Feature minibar = new Feature("Minibar");

            featureRepository.saveAll(List.of(wifi, balcony, ac, oceanView, minibar));

            Room room101 = new Room("101", "Standard", 100.00, 2);
            room101.setFeatures(Set.of(wifi, ac)); // Attach features

            Room room102 = new Room("102", "Standard", 100.00, 2);
            room102.setFeatures(Set.of(wifi, ac));

            Room room201 = new Room("201", "Deluxe", 175.00, 3);
            room201.setFeatures(Set.of(wifi, ac, balcony));

            Room room202 = new Room("202", "Deluxe", 200.00, 3);
            room202.setFeatures(Set.of(wifi, ac, balcony, oceanView));

            Room room301 = new Room("301", "Presidential Suite", 500.00, 5);
            room301.setFeatures(Set.of(wifi, ac, balcony, oceanView, minibar));

            roomRepository.saveAll(
                    List.of(room101, room102, room201, room202, room301)
            );
        };
    }
}
