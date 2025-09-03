package com.example.bankcards.controller;

import com.example.bankcards.dto.card.CardCreateDtoRequest;
import com.example.bankcards.dto.card.CardDtoResponse;
import com.example.bankcards.dto.card.CardNumberDtoRequest;
import com.example.bankcards.dto.card.TotalBalanceDtoResponse;
import com.example.bankcards.dto.page.PageDtoResponse;
import com.example.bankcards.service.CardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cards")
@RequiredArgsConstructor
public class CardController {

    private final CardService cardService;

    @PostMapping("/admin")
    public ResponseEntity<CardDtoResponse> createCard(@Valid @RequestBody CardCreateDtoRequest request) {
        CardDtoResponse response = cardService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/admin/block")
    public ResponseEntity<Void> blockCard(@Valid @RequestBody CardNumberDtoRequest request) {

        cardService.blocked(request.getNumber());
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/admin/activate")
    public ResponseEntity<Void> activateCard(@Valid @RequestBody CardNumberDtoRequest request) {

        cardService.activation(request.getNumber());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/admin")
    public ResponseEntity<Void> deleteCard(@Valid @RequestBody CardNumberDtoRequest request) {

        cardService.delete(request.getNumber());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/admin/all")
    public ResponseEntity<PageDtoResponse<CardDtoResponse>> getAllCards(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        PageDtoResponse<CardDtoResponse> response = cardService.getAll(page, size);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/my/all")
    public ResponseEntity<PageDtoResponse<CardDtoResponse>> getMyCards(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        PageDtoResponse<CardDtoResponse> response = cardService.getAllByUser(page, size);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/my")
    public ResponseEntity<CardDtoResponse> getCardByNumber(@Valid @RequestBody CardNumberDtoRequest request) {

        CardDtoResponse response = cardService.getByNumber(request.getNumber());
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/my/request-block")
    public ResponseEntity<Void> requestCardBlock(@Valid @RequestBody CardNumberDtoRequest request) {

        cardService.requestToBlocked(request.getNumber());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/my/total-balance")
    public ResponseEntity<TotalBalanceDtoResponse> getTotalBalance() {
        TotalBalanceDtoResponse response = cardService.getTotalBalanceUser();
        return ResponseEntity.ok(response);
    }
}
