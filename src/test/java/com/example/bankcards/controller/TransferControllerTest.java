package com.example.bankcards.controller;

import com.example.bankcards.dto.page.PageDtoResponse;
import com.example.bankcards.dto.transfer.TransferDtoRequest;
import com.example.bankcards.dto.transfer.TransferDtoResponse;
import com.example.bankcards.service.TransferService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransferControllerTest {

    @Mock
    private TransferService transferService;

    @InjectMocks
    private TransferController transferController;

    private TransferDtoRequest transferRequest;
    private TransferDtoResponse transferResponse;
    private PageDtoResponse<TransferDtoResponse> pageResponse;

    @BeforeEach
    void setUp() {
        transferRequest = new TransferDtoRequest();
        transferRequest.setFromCardNumber("1234567812345678");
        transferRequest.setToCardNumber("8765432187654321");
        transferRequest.setAmount(new BigDecimal("1000.00"));

        transferResponse = TransferDtoResponse.builder()
                .userEmail("user@example.com")
                .fromCardNumber("1234567812345678")
                .toCardNumber("8765432187654321")
                .amount(new BigDecimal("1000.00"))
                .time(LocalDateTime.now())
                .build();

        pageResponse = new PageDtoResponse<>(
                List.of(transferResponse),
                1L,
                1,
                0
        );
    }

    @Test
    void transferBetweenOwnCards_ValidRequest_ShouldReturnCreated() {
        when(transferService.transferBetweenCardsOneUser(any(TransferDtoRequest.class)))
                .thenReturn(transferResponse);

        ResponseEntity<TransferDtoResponse> response = transferController.transferBetweenOwnCards(transferRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("user@example.com", response.getBody().getUserEmail());
        assertEquals(new BigDecimal("1000.00"), response.getBody().getAmount());

        verify(transferService).transferBetweenCardsOneUser(transferRequest);
    }

    @Test
    void getAllTransfers_ShouldReturnOk() {
        when(transferService.getAll(anyInt(), anyInt())).thenReturn(pageResponse);

        ResponseEntity<PageDtoResponse<TransferDtoResponse>> response = transferController.getAllTransfers(0, 10);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getContent().size());
        assertEquals(1L, response.getBody().getTotalElements());

        verify(transferService).getAll(0, 10);
    }

    @Test
    void getMyTransfers_ShouldReturnOk() {
        when(transferService.getAllByUser(anyInt(), anyInt())).thenReturn(pageResponse);

        ResponseEntity<PageDtoResponse<TransferDtoResponse>> response = transferController.getMyTransfers(0, 10);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getContent().size());
        assertEquals(1L, response.getBody().getTotalElements());

        verify(transferService).getAllByUser(0, 10);
    }
}
