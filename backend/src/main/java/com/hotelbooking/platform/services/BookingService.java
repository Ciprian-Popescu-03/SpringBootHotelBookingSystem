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
import java.util.Arrays;
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
        if (!startDate.isBefore(endDate)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Start date must be before end date.");
        }


        AppUser user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Room room = roomRepository.findByIdForUpdate(roomId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Room not found"));

        boolean isOverlapping = bookingRepository.existsOverlappingBooking(roomId, startDate, endDate, blockingStatuses());
        if (isOverlapping) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Room is not available for these dates.");
        }


        Booking booking = bookingMapper.toEntity(user, room, startDate, endDate, BookingStatus.ACTIVE);
        return bookingMapper.toResponseDto(bookingRepository.save(booking));
    }

    @Transactional
    public BookingResponseDto confirmBooking(Long bookingId, AppUser currentUser) {
        Booking booking = findBookingById(bookingId);
        ensureOwnerOrAdmin(currentUser, booking);
        if (booking.getStatus() != BookingStatus.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Only active bookings can be confirmed.");
        }
        booking.setStatus(BookingStatus.CONFIRMED);
        return bookingMapper.toResponseDto(bookingRepository.save(booking));
    }

    @Transactional
    public BookingResponseDto checkInBooking(Long bookingId, AppUser currentUser) {
        Booking booking = findBookingById(bookingId);
        ensureOwnerOrAdmin(currentUser, booking);
        if (booking.getStatus() != BookingStatus.CONFIRMED && booking.getStatus() != BookingStatus.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Only active or confirmed bookings can be checked in.");
        }
        if (LocalDate.now().isBefore(booking.getStartDate())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Check-in is not available before booking start date.");
        }
        booking.setStatus(BookingStatus.CHECKED_IN);
        return bookingMapper.toResponseDto(bookingRepository.save(booking));
    }

    @Transactional
    public BookingResponseDto checkOutBooking(Long bookingId, AppUser currentUser) {
        Booking booking = findBookingById(bookingId);
        ensureOwnerOrAdmin(currentUser, booking);
        if (booking.getStatus() != BookingStatus.CHECKED_IN) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Only checked-in bookings can be checked out.");
        }
        booking.setStatus(BookingStatus.COMPLETED);
        return bookingMapper.toResponseDto(bookingRepository.save(booking));
    }

    @Transactional
    public BookingResponseDto modifyBookingDates(Long bookingId, LocalDate startDate, LocalDate endDate, AppUser currentUser) {
        if (!startDate.isBefore(endDate)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Start date must be before end date.");
        }
        Booking booking = findBookingById(bookingId);
        ensureOwnerOrAdmin(currentUser, booking);
        ensureBookingIsModifiable(booking);

        boolean isOverlapping = bookingRepository.existsOverlappingBookingExcludingBooking(
                booking.getRoom().getId(),
                bookingId,
                startDate,
                endDate,
                blockingStatuses()
        );
        if (isOverlapping) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Room is not available for these dates.");
        }
        booking.setStartDate(startDate);
        booking.setEndDate(endDate);
        if (booking.getStatus() == BookingStatus.CHECKED_IN) {
            booking.setStatus(BookingStatus.CONFIRMED);
        }
        return bookingMapper.toResponseDto(bookingRepository.save(booking));
    }

    @Transactional
    public BookingResponseDto reassignBookingRoom(Long bookingId, Long roomId, AppUser currentUser) {
        if (currentUser.getRole() != Role.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only admin can reassign booking rooms.");
        }
        Booking booking = findBookingById(bookingId);
        ensureBookingIsModifiable(booking);
        Room newRoom = roomRepository.findByIdForUpdate(roomId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Room not found"));

        boolean isOverlapping = bookingRepository.existsOverlappingBookingExcludingBooking(
                roomId,
                bookingId,
                booking.getStartDate(),
                booking.getEndDate(),
                blockingStatuses()
        );
        if (isOverlapping) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Target room is not available for this booking period.");
        }
        booking.setRoom(newRoom);
        return bookingMapper.toResponseDto(bookingRepository.save(booking));
    }

    @Transactional
    public BookingResponseDto rebook(Long sourceBookingId, Long userId, Long roomId, LocalDate startDate, LocalDate endDate) {
        Booking source = findBookingById(sourceBookingId);
        if (!source.getUser().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only rebook your own bookings.");
        }
        Long targetRoomId = roomId != null ? roomId : source.getRoom().getId();
        return createBooking(userId, targetRoomId, startDate, endDate);
    }

    @Transactional
    public void cancelBooking(Long bookingId, AppUser currentUser) {
        Booking booking = findBookingById(bookingId);
        ensureOwnerOrAdmin(currentUser, booking);
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
    public List<BookingResponseDto> getMyBookingHistory(Long userId) {
        return bookingRepository.findByUserIdOrderByStartDateDesc(userId)
                .stream()
                .map(bookingMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<BookingResponseDto> getAllBookings(AppUser currentUser) {
        if (currentUser.getRole() != Role.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only admin can view all bookings.");
        }
        return bookingRepository.findAllByOrderByStartDateAsc()
                .stream()
                .map(bookingMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<BookingResponseDto> getAllUpcomingBookings() {
        return bookingRepository.findAllByStartDateGreaterThanEqual(LocalDate.now())
                .stream()
                .filter(booking -> booking.getStatus() != BookingStatus.CANCELLED && booking.getStatus() != BookingStatus.COMPLETED)
                .map(bookingMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    private Booking findBookingById(Long bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found."));
    }

    private void ensureOwnerOrAdmin(AppUser currentUser, Booking booking) {
        boolean isOwner = booking.getUser().getId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getRole() == Role.ADMIN;
        if (!isOwner && !isAdmin) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not authorized to access this booking.");
        }
    }

    private void ensureBookingIsModifiable(Booking booking) {
        if (booking.getStatus() == BookingStatus.CANCELLED || booking.getStatus() == BookingStatus.COMPLETED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Booking cannot be modified in its current status.");
        }
    }

    private List<BookingStatus> blockingStatuses() {
        return Arrays.asList(BookingStatus.ACTIVE, BookingStatus.CONFIRMED, BookingStatus.CHECKED_IN);
    }
}
