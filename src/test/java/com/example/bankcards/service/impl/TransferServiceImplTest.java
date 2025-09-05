package com.example.bankcards.service.impl;

import com.example.bankcards.dto.transfer.TransferDtoRequest;
import com.example.bankcards.dto.transfer.TransferDtoResponse;
import com.example.bankcards.entity.card.Card;
import com.example.bankcards.entity.card.CardStatus;
import com.example.bankcards.entity.transfer.Transfer;
import com.example.bankcards.entity.user.User;
import com.example.bankcards.exception.exceptions.InvalidRequestException;
import com.example.bankcards.exception.exceptions.ResourceNotFoundException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.TransferRepository;
import com.example.bankcards.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransferServiceImplTest {

    @Mock
    private TransferRepository transferRepository;

    @Mock
    private CardRepository cardRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @Mock
    private UserDetails userDetails;

    @InjectMocks
    private TransferServiceImpl transferService;

    private User testUser;
    private Card fromCard;
    private Card toCard;
    private TransferDtoRequest validRequest;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setEmail("test@example.com");
        testUser.setName("John");
        testUser.setLastName("Doe");

        fromCard = new Card();
        fromCard.setNumber("1234567812345678");
        fromCard.setUser(testUser);
        fromCard.setStatus(CardStatus.ACTIVE);
        fromCard.setBalance(new BigDecimal("1000.00"));

        toCard = new Card();
        toCard.setNumber("8765432187654321");
        toCard.setUser(testUser);
        toCard.setStatus(CardStatus.ACTIVE);
        toCard.setBalance(new BigDecimal("500.00"));

        validRequest = new TransferDtoRequest();
        validRequest.setFromCardNumber("1234567812345678");
        validRequest.setToCardNumber("8765432187654321");
        validRequest.setAmount(new BigDecimal("100.00"));
    }

    @Test
    void transferBetweenCardsOneUser_ValidRequest_ShouldTransferSuccessfully() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn("test@example.com");

        when(cardRepository.findByNumber("1234567812345678")).thenReturn(Optional.of(fromCard));
        when(cardRepository.findByNumber("8765432187654321")).thenReturn(Optional.of(toCard));
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(transferRepository.save(any(Transfer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TransferDtoResponse response = transferService.transferBetweenCardsOneUser(validRequest);

        assertNotNull(response);
        assertEquals("test@example.com", response.getUserEmail());
        assertEquals("1234567812345678", response.getFromCardNumber());
        assertEquals("8765432187654321", response.getToCardNumber());
        assertEquals(new BigDecimal("100.00"), response.getAmount());
        assertNotNull(response.getTime());

        assertEquals(new BigDecimal("900.00"), fromCard.getBalance());
        assertEquals(new BigDecimal("600.00"), toCard.getBalance());

        verify(cardRepository, times(2)).save(any(Card.class));
        verify(transferRepository).save(any(Transfer.class));
    }

    @Test
    void transferBetweenCardsOneUser_SameCard_ShouldThrowException() {
        validRequest.setToCardNumber("1234567812345678");

        when(cardRepository.findByNumber("1234567812345678")).thenReturn(Optional.of(fromCard));
        when(cardRepository.findByNumber("1234567812345678")).thenReturn(Optional.of(toCard));

        assertThrows(InvalidRequestException.class, () -> {
            transferService.transferBetweenCardsOneUser(validRequest);
        });
    }

    @Test
    void transferBetweenCardsOneUser_CardNotFound_ShouldThrowException() {
        when(cardRepository.findByNumber("1234567812345678")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            transferService.transferBetweenCardsOneUser(validRequest);
        });

        verify(cardRepository).findByNumber("1234567812345678");
        verify(cardRepository, never()).findByNumber("8765432187654321");
    }

    @Test
    void transferBetweenCardsOneUser_CardNotBelongsToUser_ShouldThrowException() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn("test@example.com");

        User otherUser = new User();
        otherUser.setEmail("other@example.com");
        fromCard.setUser(otherUser);

        when(cardRepository.findByNumber("1234567812345678")).thenReturn(Optional.of(fromCard));
        when(cardRepository.findByNumber("8765432187654321")).thenReturn(Optional.of(toCard));
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        assertThrows(InvalidRequestException.class, () -> {
            transferService.transferBetweenCardsOneUser(validRequest);
        });

        verify(cardRepository, never()).save(any());
        verify(transferRepository, never()).save(any());
    }

    @Test
    void transferBetweenCardsOneUser_CardNotActive_ShouldThrowException() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn("test@example.com");

        fromCard.setStatus(CardStatus.BLOCKED);

        when(cardRepository.findByNumber("1234567812345678")).thenReturn(Optional.of(fromCard));
        when(cardRepository.findByNumber("8765432187654321")).thenReturn(Optional.of(toCard));
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        assertThrows(InvalidRequestException.class, () -> {
            transferService.transferBetweenCardsOneUser(validRequest);
        });

        verify(cardRepository, never()).save(any());
        verify(transferRepository, never()).save(any());
    }

    @Test
    void transferBetweenCardsOneUser_InsufficientFunds_ShouldThrowException() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn("test@example.com");

        validRequest.setAmount(new BigDecimal("1500.00"));

        when(cardRepository.findByNumber("1234567812345678")).thenReturn(Optional.of(fromCard));
        when(cardRepository.findByNumber("8765432187654321")).thenReturn(Optional.of(toCard));
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        assertThrows(InvalidRequestException.class, () -> {
            transferService.transferBetweenCardsOneUser(validRequest);
        });

        verify(cardRepository, never()).save(any());
        verify(transferRepository, never()).save(any());
    }

    @Test
    void getAll_ShouldReturnPageOfTransfers() {
        Transfer transfer = new Transfer(testUser, "1234", "5678", new BigDecimal("100.00"), LocalDateTime.now());
        Page<Transfer> page = new PageImpl<>(List.of(transfer));
        when(transferRepository.findAll(any(PageRequest.class))).thenReturn(page);

        var result = transferService.getAll(0, 10);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getTotalPages());
        assertEquals(0, result.getNumber());

        verify(transferRepository).findAll(any(PageRequest.class));
    }

    @Test
    void getAllByUser_ShouldReturnUserTransfers() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn("test@example.com");

        Transfer transfer = new Transfer(testUser, "1234", "5678", new BigDecimal("100.00"), LocalDateTime.now());
        Page<Transfer> page = new PageImpl<>(List.of(transfer));
        when(transferRepository.findAllByUser_Email(eq("test@example.com"), any(PageRequest.class))).thenReturn(page);

        var result = transferService.getAllByUser(0, 10);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("test@example.com", result.getContent().get(0).getUserEmail());

        verify(transferRepository).findAllByUser_Email(eq("test@example.com"), any(PageRequest.class));
    }
}