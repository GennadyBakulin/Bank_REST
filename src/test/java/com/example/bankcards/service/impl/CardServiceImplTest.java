package com.example.bankcards.service.impl;

import com.example.bankcards.dto.card.CardCreateDtoRequest;
import com.example.bankcards.dto.card.CardDtoResponse;
import com.example.bankcards.dto.card.TotalBalanceDtoResponse;
import com.example.bankcards.dto.page.PageDtoResponse;
import com.example.bankcards.entity.card.Card;
import com.example.bankcards.entity.card.CardStatus;
import com.example.bankcards.entity.user.Role;
import com.example.bankcards.entity.user.User;
import com.example.bankcards.exception.exceptions.ConflictRequestException;
import com.example.bankcards.exception.exceptions.InvalidRequestException;
import com.example.bankcards.exception.exceptions.ResourceNotFoundException;
import com.example.bankcards.repository.CardRepository;
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
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardServiceImplTest {

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
    private CardServiceImpl cardService;

    private User testUser;
    private Card testCard;
    private CardCreateDtoRequest createRequest;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setEmail("test@example.com");
        testUser.setName("John");
        testUser.setLastName("Doe");
        testUser.setRole(Role.USER);

        testCard = new Card();
        testCard.setNumber("1234567812345678");
        testCard.setUser(testUser);
        testCard.setFullNameUser("John Doe");
        testCard.setExpirationDate(LocalDate.now().plusMonths(12));
        testCard.setStatus(CardStatus.ACTIVE);
        testCard.setBalance(new BigDecimal("1000.00"));

        createRequest = new CardCreateDtoRequest();
        createRequest.setNumber("1234567812345678");
        createRequest.setUserEmail("test@example.com");
        createRequest.setCountMonth(12);
        createRequest.setAmount(new BigDecimal("1000.00"));
    }

    @Test
    void create_ValidRequest_ShouldCreateCard() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(cardRepository.existsByNumber("1234567812345678")).thenReturn(false);
        when(cardRepository.save(any(Card.class))).thenReturn(testCard);

        CardDtoResponse response = cardService.create(createRequest);

        assertNotNull(response);
        assertEquals("**** **** **** 5678", response.getMaskedCardNumber());
        assertEquals("test@example.com", response.getEmail());
        assertEquals("John Doe", response.getFullNameUser());
        assertEquals(CardStatus.ACTIVE, response.getStatus());
        assertEquals(new BigDecimal("1000.00"), response.getBalance());

        verify(userRepository).findByEmail("test@example.com");
        verify(cardRepository).existsByNumber("1234567812345678");
        verify(cardRepository).save(any(Card.class));
    }

    @Test
    void create_InvalidCardNumber_ShouldThrowException() {
        createRequest.setNumber("invalid");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        assertThrows(InvalidRequestException.class, () -> {
            cardService.create(createRequest);
        });

        verify(userRepository).findByEmail(any());
        verify(cardRepository, never()).existsByNumber(any());
        verify(cardRepository, never()).save(any());
    }

    @Test
    void create_DuplicateCardNumber_ShouldThrowException() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(cardRepository.existsByNumber("1234567812345678")).thenReturn(true);

        assertThrows(ConflictRequestException.class, () -> {
            cardService.create(createRequest);
        });

        verify(userRepository).findByEmail("test@example.com");
        verify(cardRepository).existsByNumber("1234567812345678");
        verify(cardRepository, never()).save(any());
    }

    @Test
    void blocked_CardExists_ShouldBlockCard() {
        when(cardRepository.findByNumber("1234567812345678")).thenReturn(Optional.of(testCard));
        when(cardRepository.save(any(Card.class))).thenReturn(testCard);

        cardService.blocked("1234567812345678");

        assertEquals(CardStatus.BLOCKED, testCard.getStatus());
        verify(cardRepository).findByNumber("1234567812345678");
        verify(cardRepository).save(testCard);
    }

    @Test
    void activation_ValidCard_ShouldActivateCard() {
        testCard.setStatus(CardStatus.BLOCKED);
        when(cardRepository.findByNumber("1234567812345678")).thenReturn(Optional.of(testCard));
        when(cardRepository.save(any(Card.class))).thenReturn(testCard);

        cardService.activation("1234567812345678");

        assertEquals(CardStatus.ACTIVE, testCard.getStatus());
        verify(cardRepository).findByNumber("1234567812345678");
        verify(cardRepository).save(testCard);
    }

    @Test
    void activation_ExpiredCard_ShouldThrowException() {
        testCard.setExpirationDate(LocalDate.now().minusDays(1));
        testCard.setStatus(CardStatus.EXPIRED);
        when(cardRepository.findByNumber("1234567812345678")).thenReturn(Optional.of(testCard));

        assertThrows(ConflictRequestException.class, () -> {
            cardService.activation("1234567812345678");
        });

        verify(cardRepository).findByNumber("1234567812345678");
        verify(cardRepository, never()).save(any());
    }

    @Test
    void delete_CardExists_ShouldDeleteCard() {
        when(cardRepository.findByNumber("1234567812345678")).thenReturn(Optional.of(testCard));
        doNothing().when(cardRepository).delete(testCard);

        cardService.delete("1234567812345678");

        verify(cardRepository).findByNumber("1234567812345678");
        verify(cardRepository).delete(testCard);
    }

    @Test
    void getAll_ShouldReturnPageOfCards() {
        Page<Card> cardPage = new PageImpl<>(List.of(testCard));
        when(cardRepository.findAll(any(PageRequest.class))).thenReturn(cardPage);

        PageDtoResponse<CardDtoResponse> result = cardService.getAll(0, 10);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(cardRepository).findAll(any(PageRequest.class));
    }

    @Test
    void getAllByUser_ShouldReturnUserCards() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn("test@example.com");

        Page<Card> cardPage = new PageImpl<>(List.of(testCard));
        when(cardRepository.findAllByUser_Email(eq("test@example.com"), any(PageRequest.class))).thenReturn(cardPage);

        PageDtoResponse<CardDtoResponse> result = cardService.getAllByUser(0, 10);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(cardRepository).findAllByUser_Email(eq("test@example.com"), any(PageRequest.class));
    }

    @Test
    void getByNumber_UserOwnsCard_ShouldReturnCard() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn("test@example.com");

        when(cardRepository.findByNumber("1234567812345678")).thenReturn(Optional.of(testCard));

        CardDtoResponse result = cardService.getByNumber("1234567812345678");

        assertNotNull(result);
        assertEquals("**** **** **** 5678", result.getMaskedCardNumber());
        verify(cardRepository).findByNumber("1234567812345678");
    }

    @Test
    void getByNumber_UserDoesNotOwnCard_ShouldThrowException() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn("test@example.com");

        User otherUser = new User();
        otherUser.setEmail("other@example.com");
        testCard.setUser(otherUser);

        when(cardRepository.findByNumber("1234567812345678")).thenReturn(Optional.of(testCard));

        assertThrows(ResourceNotFoundException.class, () -> {
            cardService.getByNumber("1234567812345678");
        });

        verify(cardRepository).findByNumber("1234567812345678");
    }

    @Test
    void requestToBlocked_ValidCard_ShouldSetRequestToBlocked() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn("test@example.com");

        when(cardRepository.findByNumber("1234567812345678")).thenReturn(Optional.of(testCard));
        when(cardRepository.save(any(Card.class))).thenReturn(testCard);

        cardService.requestToBlocked("1234567812345678");

        assertTrue(testCard.getRequestToBlocked());
        verify(cardRepository).findByNumber("1234567812345678");
        verify(cardRepository).save(testCard);
    }

    @Test
    void getTotalBalanceUser_ShouldReturnTotalBalance() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn("test@example.com");

        Card card1 = new Card();
        card1.setStatus(CardStatus.ACTIVE);
        card1.setBalance(new BigDecimal("500.00"));

        Card card2 = new Card();
        card2.setStatus(CardStatus.ACTIVE);
        card2.setBalance(new BigDecimal("300.00"));

        Card inactiveCard = new Card();
        inactiveCard.setStatus(CardStatus.BLOCKED);
        inactiveCard.setBalance(new BigDecimal("200.00"));

        when(cardRepository.findAllByUser_Email("test@example.com")).thenReturn(List.of(card1, card2, inactiveCard));

        TotalBalanceDtoResponse result = cardService.getTotalBalanceUser();

        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
        assertEquals(new BigDecimal("800.00"), result.getTotalBalance());
        verify(cardRepository).findAllByUser_Email("test@example.com");
    }

    @Test
    void checkExpiredCard_CardExpired_ShouldUpdateStatus() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn("test@example.com");

        testCard.setExpirationDate(LocalDate.now().minusDays(1));
        testCard.setStatus(CardStatus.ACTIVE);
        when(cardRepository.save(any(Card.class))).thenReturn(testCard);
        when(cardRepository.findByNumber(testCard.getNumber())).thenReturn(Optional.of(testCard));

        CardDtoResponse result = cardService.getByNumber(testCard.getNumber());

        assertEquals(CardStatus.EXPIRED, result.getStatus());
        verify(cardRepository).save(testCard);
    }

    @Test
    void findCardByNumber_CardNotExists_ShouldThrowException() {
        when(cardRepository.findByNumber("9999999999999999")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            cardService.getByNumber("9999999999999999");
        });
    }
}
