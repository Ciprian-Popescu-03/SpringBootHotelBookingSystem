package com.hotelbooking.platform.controllers;

import com.hotelbooking.platform.dto.request.BookingRequestDto;
import com.hotelbooking.platform.dto.request.ModifyBookingDatesRequest;
import com.hotelbooking.platform.dto.request.ReassignBookingRoomRequest;
import com.hotelbooking.platform.dto.request.RebookRequest;
import com.hotelbooking.platform.dto.response.BookingResponseDto;
import com.hotelbooking.platform.entities.AppUser;
import com.hotelbooking.platform.entities.Role;
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

    @GetMapping("/my/history")
    public ResponseEntity<List<BookingResponseDto>> getMyBookingHistory(@AuthenticationPrincipal AppUser currentUser) {
        List<BookingResponseDto> myHistory = bookingService.getMyBookingHistory(currentUser.getId());
        return ResponseEntity.ok(myHistory);
    }

    @GetMapping
    public ResponseEntity<List<BookingResponseDto>> getAllBookings(@AuthenticationPrincipal AppUser currentUser) {
        return ResponseEntity.ok(bookingService.getAllBookings(currentUser));
    }

    @GetMapping("/upcoming")
    public ResponseEntity<List<BookingResponseDto>> getUpcomingBookings(@AuthenticationPrincipal AppUser currentUser) {
        if (currentUser.getRole() != Role.ADMIN) {
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN, "You do not have permission to view all upcoming bookings.");
        }

        List<BookingResponseDto> upcomingBookings = bookingService.getAllUpcomingBookings();
        return ResponseEntity.ok(upcomingBookings);
    }

    @PatchMapping("/{id}/confirm")
    public ResponseEntity<BookingResponseDto> confirmBooking(@PathVariable Long id, @AuthenticationPrincipal AppUser currentUser) {
        return ResponseEntity.ok(bookingService.confirmBooking(id, currentUser));
    }

    @PatchMapping("/{id}/check-in")
    public ResponseEntity<BookingResponseDto> checkInBooking(@PathVariable Long id, @AuthenticationPrincipal AppUser currentUser) {
        return ResponseEntity.ok(bookingService.checkInBooking(id, currentUser));
    }

    @PatchMapping("/{id}/check-out")
    public ResponseEntity<BookingResponseDto> checkOutBooking(@PathVariable Long id, @AuthenticationPrincipal AppUser currentUser) {
        return ResponseEntity.ok(bookingService.checkOutBooking(id, currentUser));
    }

    @PatchMapping("/{id}/dates")
    public ResponseEntity<BookingResponseDto> modifyBookingDates(
            @PathVariable Long id,
            @Valid @RequestBody ModifyBookingDatesRequest request,
            @AuthenticationPrincipal AppUser currentUser
    ) {
        return ResponseEntity.ok(bookingService.modifyBookingDates(id, request.startDate(), request.endDate(), currentUser));
    }

    @PostMapping("/{id}/rebook")
    public ResponseEntity<BookingResponseDto> rebook(
            @PathVariable Long id,
            @Valid @RequestBody RebookRequest request,
            @AuthenticationPrincipal AppUser currentUser
    ) {
        BookingResponseDto response = bookingService.rebook(id, currentUser.getId(), request.roomId(), request.startDate(), request.endDate());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{id}/reassign-room")
    public ResponseEntity<BookingResponseDto> reassignRoom(
            @PathVariable Long id,
            @Valid @RequestBody ReassignBookingRoomRequest request,
            @AuthenticationPrincipal AppUser currentUser
    ) {
        return ResponseEntity.ok(bookingService.reassignBookingRoom(id, request.roomId(), currentUser));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelBooking(@PathVariable Long id, @AuthenticationPrincipal AppUser currentUser) {
        bookingService.cancelBooking(id, currentUser);
        return ResponseEntity.noContent().build();
    }

}
