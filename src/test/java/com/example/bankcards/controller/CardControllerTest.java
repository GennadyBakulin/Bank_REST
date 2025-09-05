package com.example.bankcards.controller;

import com.example.bankcards.dto.card.CardCreateDtoRequest;
import com.example.bankcards.dto.card.CardDtoResponse;
import com.example.bankcards.dto.card.CardNumberDtoRequest;
import com.example.bankcards.dto.card.TotalBalanceDtoResponse;
import com.example.bankcards.dto.page.PageDtoResponse;
import com.example.bankcards.entity.card.CardStatus;
import com.example.bankcards.service.CardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardControllerTest {

    @Mock
    private CardService cardService;

    @InjectMocks
    private CardController cardController;

    private CardCreateDtoRequest createRequest;
    private CardNumberDtoRequest numberRequest;
    private CardDtoResponse cardResponse;
    private TotalBalanceDtoResponse balanceResponse;
    private PageDtoResponse<CardDtoResponse> pageResponse;

    @BeforeEach
    void setUp() {
        createRequest = new CardCreateDtoRequest();
        createRequest.setNumber("1234567812345678");
        createRequest.setUserEmail("user@example.com");
        createRequest.setCountMonth(36);
        createRequest.setAmount(new BigDecimal("1000.00"));

        numberRequest = new CardNumberDtoRequest();
        numberRequest.setNumber("1234567812345678");

        cardResponse = CardDtoResponse.builder()
                .maskedCardNumber("**** **** **** 5678")
                .email("user@example.com")
                .fullNameUser("John Doe")
                .expirationDate(LocalDate.now().plusMonths(36))
                .status(CardStatus.ACTIVE)
                .balance(new BigDecimal("1000.00"))
                .build();

        balanceResponse = TotalBalanceDtoResponse.builder()
                .email("user@example.com")
                .totalBalance(new BigDecimal("1500.50"))
                .build();

        pageResponse = new PageDtoResponse<>(
                List.of(cardResponse),
                1L,
                1,
                0
        );
    }

    @Test
    void createCard_ValidRequest_ShouldReturnCreated() {
        when(cardService.create(any(CardCreateDtoRequest.class))).thenReturn(cardResponse);

        ResponseEntity<CardDtoResponse> response = cardController.createCard(createRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("**** **** **** 5678", response.getBody().getMaskedCardNumber());
        assertEquals("user@example.com", response.getBody().getEmail());

        verify(cardService).create(createRequest);
    }

    @Test
    void blockCard_ValidRequest_ShouldReturnOk() {
        doNothing().when(cardService).blocked(anyString());

        ResponseEntity<Void> response = cardController.blockCard(numberRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNull(response.getBody());

        verify(cardService).blocked("1234567812345678");
    }

    @Test
    void activateCard_ValidRequest_ShouldReturnOk() {
        doNothing().when(cardService).activation(anyString());

        ResponseEntity<Void> response = cardController.activateCard(numberRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNull(response.getBody());

        verify(cardService).activation("1234567812345678");
    }

    @Test
    void deleteCard_ValidRequest_ShouldReturnNoContent() {
        doNothing().when(cardService).delete(anyString());

        ResponseEntity<Void> response = cardController.deleteCard(numberRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNull(response.getBody());

        verify(cardService).delete("1234567812345678");
    }

    @Test
    void getAllCards_ShouldReturnOk() {
        when(cardService.getAll(anyInt(), anyInt())).thenReturn(pageResponse);

        ResponseEntity<PageDtoResponse<CardDtoResponse>> response = cardController.getAllCards(0, 10);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getContent().size());

        verify(cardService).getAll(0, 10);
    }

    @Test
    void getMyCards_ShouldReturnOk() {
        when(cardService.getAllByUser(anyInt(), anyInt())).thenReturn(pageResponse);

        ResponseEntity<PageDtoResponse<CardDtoResponse>> response = cardController.getMyCards(0, 10);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getContent().size());

        verify(cardService).getAllByUser(0, 10);
    }

    @Test
    void getCardByNumber_ValidRequest_ShouldReturnOk() {
        when(cardService.getByNumber(anyString())).thenReturn(cardResponse);

        ResponseEntity<CardDtoResponse> response = cardController.getCardByNumber(numberRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("**** **** **** 5678", response.getBody().getMaskedCardNumber());

        verify(cardService).getByNumber("1234567812345678");
    }

    @Test
    void requestCardBlock_ValidRequest_ShouldReturnOk() {
        doNothing().when(cardService).requestToBlocked(anyString());

        ResponseEntity<Void> response = cardController.requestCardBlock(numberRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNull(response.getBody());

        verify(cardService).requestToBlocked("1234567812345678");
    }

    @Test
    void getAllCards_WithCustomPagination_ShouldUseCorrectParameters() {
        when(cardService.getAll(anyInt(), anyInt())).thenReturn(pageResponse);

        ResponseEntity<PageDtoResponse<CardDtoResponse>> response = cardController.getAllCards(2, 25);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        verify(cardService).getAll(2, 25);
    }

    @Test
    void getMyCards_WithCustomPagination_ShouldUseCorrectParameters() {
        when(cardService.getAllByUser(anyInt(), anyInt())).thenReturn(pageResponse);

        ResponseEntity<PageDtoResponse<CardDtoResponse>> response = cardController.getMyCards(1, 5);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        verify(cardService).getAllByUser(1, 5);
    }

    @Test
    void getAllCards_EmptyPage_ShouldReturnEmptyPage() {
        PageDtoResponse<CardDtoResponse> emptyPage = new PageDtoResponse<>(
                List.of(),
                0L,
                0,
                0
        );
        when(cardService.getAll(anyInt(), anyInt())).thenReturn(emptyPage);

        ResponseEntity<PageDtoResponse<CardDtoResponse>> response = cardController.getAllCards(0, 10);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(0, response.getBody().getContent().size());

        verify(cardService).getAll(0, 10);
    }

    @Test
    void getTotalBalance_ShouldReturnOk() {
        when(cardService.getTotalBalanceUser()).thenReturn(balanceResponse);

        ResponseEntity<TotalBalanceDtoResponse> response = cardController.getTotalBalance();

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("user@example.com", response.getBody().getEmail());
        assertEquals(new BigDecimal("1500.50"), response.getBody().getTotalBalance());

        verify(cardService).getTotalBalanceUser();
    }
}
