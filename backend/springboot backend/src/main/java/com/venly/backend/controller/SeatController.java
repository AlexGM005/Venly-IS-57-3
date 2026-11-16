package com.venly.backend.controller;

import com.venly.backend.dto.SeatMapResponse;
import com.venly.backend.service.SeatService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/funciones")
public class SeatController {

    private static final String SESSION_HEADER = ProfileController.SESSION_HEADER;

    private final SeatService seatService;

    public SeatController(SeatService seatService) {
        this.seatService = seatService;
    }

    @GetMapping("/{peliculaId}/asientos")
    public ResponseEntity<SeatMapResponse> getSeatMap(@RequestHeader(SESSION_HEADER) Long requesterId,
                                                      @PathVariable Long peliculaId) {
        System.out.println("\n[VENLY TRACE - BACKEND] 🔵 Acción: Petición GET /api/funciones/{peliculaId}/asientos\nDe: SeatController.java -> Hacia: SeatServiceImpl.java");
        return ResponseEntity.ok(seatService.getSeatMap(requesterId, peliculaId));
    }
}
