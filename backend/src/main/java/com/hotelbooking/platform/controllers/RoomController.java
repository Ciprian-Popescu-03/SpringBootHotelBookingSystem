package com.hotelbooking.platform.controllers;

import com.hotelbooking.platform.dto.request.RoomRequestDto;
import com.hotelbooking.platform.dto.response.RoomResponseDto;
import com.hotelbooking.platform.services.RoomService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping(path = "/api/rooms")
public class RoomController {

    private final RoomService roomService;

    @Autowired
    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @GetMapping
    public ResponseEntity<Page<RoomResponseDto>> getRooms(
            @RequestParam(required = false) String roomType,
            @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        Page<RoomResponseDto> rooms = roomService.getRooms(roomType, pageable);
        return ResponseEntity.ok(rooms);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<RoomResponseDto> registerNewRoom(@Valid @RequestBody RoomRequestDto roomDTO){
        RoomResponseDto savedRoom = roomService.addNewRoom(roomDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedRoom);
    }

    @GetMapping(path = "{id}")
    public ResponseEntity<RoomResponseDto> getRoomById(@PathVariable("id") Long id) {
        RoomResponseDto room = roomService.getRoomById(id);
        return ResponseEntity.ok(room);
    }

    @PutMapping(path = "{id}")
    public ResponseEntity<RoomResponseDto> updateRoom(@PathVariable("id") Long id, @Valid @RequestBody RoomRequestDto roomDTO) {
        RoomResponseDto updatedRoom = roomService.updateRoom(id, roomDTO);
        return ResponseEntity.ok(updatedRoom);
    }

    @DeleteMapping(path = "{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> deleteRoom(@PathVariable("id") Long id) {
        roomService.deleteRoom(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public ResponseEntity<List<RoomResponseDto>> searchRooms(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String roomType,
            @RequestParam(required = false) Integer capacity
    ) {
        if (!startDate.isBefore(endDate)) {
            return ResponseEntity.badRequest().build();
        }
        List<RoomResponseDto> availableRooms = roomService.searchAvailableRooms(startDate, endDate, roomType, capacity);

        return ResponseEntity.ok(availableRooms);
    }
}
