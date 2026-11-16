package com.venly.backend.controller;

import com.venly.backend.dto.CreateReservationRequest;
import com.venly.backend.dto.ReservationResponse;
import com.venly.backend.service.ReservationService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reservas")
public class ReservationController {

    private static final String SESSION_HEADER = ProfileController.SESSION_HEADER;

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> create(@RequestHeader(SESSION_HEADER) Long requesterId,
                                                      @Valid @RequestBody CreateReservationRequest request) {
        System.out.println("\n[VENLY TRACE - BACKEND] 🔵 Acción: Petición POST /api/reservas (Cliente)\nDe: ReservationController.java -> Hacia: ReservationServiceImpl.java");
        return ResponseEntity.status(HttpStatus.CREATED).body(reservationService.create(requesterId, request));
    }

    @GetMapping
    public ResponseEntity<List<ReservationResponse>> findAll(@RequestHeader(SESSION_HEADER) Long requesterId) {
        System.out.println("\n[VENLY TRACE - BACKEND] 🔵 Acción: Petición GET /api/reservas (Cliente vs Staff)\nDe: ReservationController.java -> Hacia: ReservationServiceImpl.java");
        return ResponseEntity.ok(reservationService.findAll(requesterId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReservationResponse> findById(@RequestHeader(SESSION_HEADER) Long requesterId,
                                                        @PathVariable Long id) {
        System.out.println("\n[VENLY TRACE - BACKEND] 🔵 Acción: Petición GET /api/reservas/{id}\nDe: ReservationController.java -> Hacia: ReservationServiceImpl.java");
        return ResponseEntity.ok(reservationService.findById(requesterId, id));
    }
}
