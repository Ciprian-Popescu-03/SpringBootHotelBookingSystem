package com.hotelbooking.platform.repositories;

import com.hotelbooking.platform.entities.Booking;
import com.hotelbooking.platform.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    // overlap logic
    @Query("SELECT COUNT(b) > 0 FROM Booking b WHERE b.room.id = :roomId " +
            "AND b.startDate < :requestedEnd " +
            "AND b.endDate > :requestedStart " +
            "AND b.status <> :excludedStatus")
    boolean existsOverlappingBooking(
            @Param("roomId") Long roomId,
            @Param("requestedStart") LocalDate requestedStart,
            @Param("requestedEnd") LocalDate requestedEnd,
            @Param("excludedStatus") BookingStatus excludedStatus
    );

    // Required for the "GET /bookings/my" endpoint
    List<Booking> findByUserId(Long userId);

    // Required for the "Admin" upcoming bookings requirement
    List<Booking> findAllByStartDateGreaterThanEqual(LocalDate currentDate);
}
