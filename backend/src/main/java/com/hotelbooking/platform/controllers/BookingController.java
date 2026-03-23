package com.hotelbooking.platform.controllers;

import com.hotelbooking.platform.dto.request.BookingRequestDto;
import com.hotelbooking.platform.dto.response.BookingResponseDto;
import com.hotelbooking.platform.entities.AppUser;
import com.hotelbooking.platform.services.BookingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public ResponseEntity<BookingResponseDto> createBooking(
            @Valid @RequestBody BookingRequestDto dto,
            @AuthenticationPrincipal AppUser currentUser) {
        Long realUserId = currentUser.getId();
        BookingResponseDto response = bookingService.createBooking(realUserId, dto.roomId(), dto.startDate(), dto.endDate());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/my")
    public ResponseEntity<List<BookingResponseDto>> getMyBookings(@AuthenticationPrincipal AppUser currentUser) {
        Long realUserId = currentUser.getId();
        List<BookingResponseDto> myBookings = bookingService.getMyBookings(realUserId);
        return ResponseEntity.ok(myBookings);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelBooking(@PathVariable Long id, @AuthenticationPrincipal AppUser currentUser) {
        bookingService.cancelBooking(id, currentUser);
        return ResponseEntity.noContent().build();
    }
}
