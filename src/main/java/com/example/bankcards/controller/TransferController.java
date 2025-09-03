package com.example.bankcards.controller;

import com.example.bankcards.dto.page.PageDtoResponse;
import com.example.bankcards.dto.transfer.TransferDtoRequest;
import com.example.bankcards.dto.transfer.TransferDtoResponse;
import com.example.bankcards.service.TransferService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transfers")
@RequiredArgsConstructor
public class TransferController {

    private final TransferService transferService;

    @PostMapping("/my/between-cards")
    public ResponseEntity<TransferDtoResponse> transferBetweenOwnCards(
            @Valid @RequestBody TransferDtoRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(transferService.transferBetweenCardsOneUser(request));
    }

    @GetMapping("/all")
    public ResponseEntity<PageDtoResponse<TransferDtoResponse>> getAllTransfers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(transferService.getAll(page, size));
    }

    @GetMapping("/my")
    public ResponseEntity<PageDtoResponse<TransferDtoResponse>> getMyTransfers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(transferService.getAllByUser(page, size));
    }
}
