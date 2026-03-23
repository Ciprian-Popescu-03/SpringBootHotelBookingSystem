package com.hotelbooking.platform.mappers;

import com.hotelbooking.platform.dto.response.BookingResponseDto;
import com.hotelbooking.platform.entities.AppUser;
import com.hotelbooking.platform.entities.Booking;
import com.hotelbooking.platform.entities.Room;
import com.hotelbooking.platform.enums.BookingStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class BookingMapper {

    public Booking toEntity(AppUser user, Room room, LocalDate startDate, LocalDate endDate, BookingStatus status) {
        Booking booking = new Booking();
        booking.setUser(user);
        booking.setRoom(room);
        booking.setStartDate(startDate);
        booking.setEndDate(endDate);
        booking.setStatus(status);
        return booking;
    }

    public BookingResponseDto toResponseDto(Booking booking) {
        return new BookingResponseDto(
                booking.getId(),
                booking.getUser().getId(),
                booking.getRoom().getId(),
                booking.getStartDate(),
                booking.getEndDate(),
                booking.getStatus()
        );
    }
}
