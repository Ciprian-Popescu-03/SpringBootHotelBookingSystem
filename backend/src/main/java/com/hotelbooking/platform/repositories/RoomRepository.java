package com.hotelbooking.platform.repositories;

import com.hotelbooking.platform.entities.Room;
import com.hotelbooking.platform.enums.BookingStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {

    @Query("SELECT r FROM Room r WHERE NOT EXISTS (" +
            "SELECT b FROM Booking b WHERE b.room = r " +
            "AND b.startDate < :endDate AND b.endDate > :startDate " +
            "AND b.status <> :excludedStatus) " +
            "AND (:roomType IS NULL OR r.roomType = :roomType) " +
            "AND (:capacity IS NULL OR r.capacity >= :capacity)")
    List<Room> findAvailableRooms(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("roomType") String roomType,
            @Param("capacity") Integer capacity,
            @Param("excludedStatus") BookingStatus excludedStatus
    );

    Page<Room> findByRoomType(String type, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM Room r WHERE r.id = :id")
    Optional<Room> findByIdForUpdate(@Param("id") Long id);
}
