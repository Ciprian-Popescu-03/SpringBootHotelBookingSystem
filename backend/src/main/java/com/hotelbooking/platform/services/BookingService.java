package com.hotelbooking.platform.services;

import com.hotelbooking.platform.dto.response.BookingResponseDto;
import com.hotelbooking.platform.entities.AppUser;
import com.hotelbooking.platform.entities.Booking;
import com.hotelbooking.platform.entities.Role;
import com.hotelbooking.platform.entities.Room;
import com.hotelbooking.platform.enums.BookingStatus;
import com.hotelbooking.platform.mappers.BookingMapper;
import com.hotelbooking.platform.repositories.BookingRepository;
import com.hotelbooking.platform.repositories.RoomRepository;
import com.hotelbooking.platform.repositories.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final RoomRepository roomRepository;
    private final BookingMapper bookingMapper;


    public BookingService(BookingRepository bookingRepository, UserRepository userRepository, RoomRepository roomRepository, BookingMapper bookingMapper) {
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.roomRepository = roomRepository;
        this.bookingMapper = bookingMapper;
    }


    @Transactional
    public BookingResponseDto createBooking(Long userId, Long roomId, LocalDate startDate, LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Start date must be before end date.");
        }


        AppUser user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Room room = roomRepository.findByIdForUpdate(roomId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Room not found"));

        boolean isOverlapping = bookingRepository.existsOverlappingBooking(roomId, startDate, endDate, BookingStatus.CANCELLED);
        if (isOverlapping) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Room is not available for these dates.");
        }


        Booking booking = bookingMapper.toEntity(user, room, startDate, endDate, BookingStatus.ACTIVE);
        return bookingMapper.toResponseDto(bookingRepository.save(booking));
    }

    @Transactional
    public void cancelBooking(Long bookingId, AppUser currentUser) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found."));
        boolean isOwner = booking.getUser().getId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getRole() == Role.ADMIN;
        if (!isOwner && !isAdmin) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not authorized to cancel this booking.");
        }
        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);
    }

    @Transactional(readOnly = true)
    public List<BookingResponseDto> getMyBookings(Long userId) {
        return bookingRepository.findByUserId(userId)
                .stream()
                .map(bookingMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<Booking> getAllUpcomingBookings() {
        return bookingRepository.findAllByStartDateGreaterThanEqual(LocalDate.now());
    }
}
