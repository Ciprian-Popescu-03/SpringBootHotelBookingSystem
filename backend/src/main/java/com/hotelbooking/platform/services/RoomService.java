package com.hotelbooking.platform.services;

import com.hotelbooking.platform.dto.request.RoomRequestDto;
import com.hotelbooking.platform.dto.response.RoomResponseDto;
import com.hotelbooking.platform.exceptions.RoomNotFoundException;
import com.hotelbooking.platform.entities.Feature;
import com.hotelbooking.platform.entities.Room;
import com.hotelbooking.platform.enums.BookingStatus;
import com.hotelbooking.platform.mappers.RoomMapper;
import com.hotelbooking.platform.repositories.FeatureRepository;
import com.hotelbooking.platform.repositories.RoomRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RoomService {

    private final RoomRepository roomRepository;
    private final FeatureRepository featureRepository;
    private final RoomMapper roomMapper;

    public RoomService(RoomRepository roomRepository, FeatureRepository featureRepository, RoomMapper roomMapper) {
        this.roomRepository = roomRepository;
        this.featureRepository = featureRepository;
        this.roomMapper = roomMapper;
    }

    @Transactional(readOnly = true)
    public Page<RoomResponseDto> getRooms(String roomType, Pageable pageable) {
        Page<Room> rooms;
        if (roomType != null && !roomType.isBlank()) {
            rooms = roomRepository.findByRoomType(roomType, pageable);
        } else {
            rooms = roomRepository.findAll(pageable);
        }

        return rooms.map(roomMapper::toResponseDto);
    }

    public RoomResponseDto addNewRoom(RoomRequestDto roomDTO) {
        Room room = roomMapper.toEntity(roomDTO);

        Set<Feature> managedFeatures = processFeatures(room.getFeatures());
        room.setFeatures(managedFeatures);

        Room savedRoom = roomRepository.save(room);
        return roomMapper.toResponseDto(savedRoom);
    }

    @Transactional(readOnly = true)
    public RoomResponseDto getRoomById(Long id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new RoomNotFoundException("Room with id " + id + " not found"));
        return roomMapper.toResponseDto(room);
    }

    public RoomResponseDto updateRoom(Long id, RoomRequestDto roomDTO) {
        Room existingRoom = roomRepository.findById(id)
                .orElseThrow(() -> new RoomNotFoundException("Room with id " + id + " not found"));

        roomMapper.updateEntity(existingRoom, roomDTO);

        Room mappedRoom = roomMapper.toEntity(roomDTO);
        Set<Feature> managedFeatures = processFeatures(mappedRoom.getFeatures());
        existingRoom.setFeatures(managedFeatures);

        Room updatedRoom = roomRepository.save(existingRoom);
        return roomMapper.toResponseDto(updatedRoom);
    }

    public void deleteRoom(Long id) {
        if (!roomRepository.existsById(id)) {
            throw new RoomNotFoundException("Room with id " + id + " not found");
        }
        roomRepository.deleteById(id);
    }

    private Set<Feature> processFeatures(Set<Feature> featuresFromRequest) {
        Set<Feature> managedFeatures = new HashSet<>();

        if (featuresFromRequest != null) {
            for (Feature feature : featuresFromRequest) {
                Feature existingFeature = featureRepository.findByName(feature.getName())
                        .orElse(null);

                if (existingFeature != null) {
                    managedFeatures.add(existingFeature);
                } else {
                    Feature savedFeature = featureRepository.save(feature);
                    managedFeatures.add(savedFeature);
                }
            }
        }
        return managedFeatures;
    }

    @Transactional(readOnly = true)
    public List<RoomResponseDto> searchAvailableRooms(
            LocalDate startDate,
            LocalDate endDate,
            String roomType,
            Integer capacity,
            Double minPrice,
            Double maxPrice
    ) {
        List<Room> rooms = roomRepository.findAvailableRooms(
                startDate,
                endDate,
                roomType,
                capacity,
                minPrice,
                maxPrice,
                Arrays.asList(BookingStatus.ACTIVE, BookingStatus.CONFIRMED, BookingStatus.CHECKED_IN)
        );
        return rooms.stream().map(roomMapper::toResponseDto).collect(Collectors.toList());
    }
}
