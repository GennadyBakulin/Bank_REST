package com.example.bankcards.service.impl;

import com.example.bankcards.dto.page.PageDtoResponse;
import com.example.bankcards.dto.user.UserDtoResponse;
import com.example.bankcards.entity.user.Role;
import com.example.bankcards.entity.user.User;
import com.example.bankcards.exception.exceptions.ResourceNotFoundException;
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

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setEmail("test@example.com");
        testUser.setName("John");
        testUser.setLastName("Doe");
        testUser.setRole(Role.USER);
    }

    @Test
    void getUserByEmail_UserExists_ShouldReturnUserDto() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        UserDtoResponse result = userService.getUserByEmail("test@example.com");

        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
        assertEquals("John", result.getName());
        assertEquals("Doe", result.getLastName());
        assertEquals(Role.USER, result.getRole());

        verify(userRepository).findByEmail("test@example.com");
    }

    @Test
    void getUserByEmail_UserNotFound_ShouldThrowException() {
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            userService.getUserByEmail("nonexistent@example.com");
        });

        verify(userRepository).findByEmail("nonexistent@example.com");
    }

    @Test
    void getAllUsers_ShouldReturnPageOfUsers() {
        User user1 = new User();
        user1.setEmail("user1@example.com");
        user1.setName("Alice");
        user1.setLastName("Smith");
        user1.setRole(Role.USER);

        User user2 = new User();
        user2.setEmail("user2@example.com");
        user2.setName("Bob");
        user2.setLastName("Johnson");
        user2.setRole(Role.ADMIN);

        Page<User> userPage = new PageImpl<>(List.of(user1, user2));
        when(userRepository.findAll(any(PageRequest.class))).thenReturn(userPage);

        PageDtoResponse<UserDtoResponse> result = userService.getAllUsers(0, 10);

        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        assertEquals(2, result.getTotalElements());
        assertEquals(1, result.getTotalPages());
        assertEquals(0, result.getNumber());

        UserDtoResponse firstUser = result.getContent().get(0);
        assertEquals("user1@example.com", firstUser.getEmail());
        assertEquals("Alice", firstUser.getName());
        assertEquals("Smith", firstUser.getLastName());
        assertEquals(Role.USER, firstUser.getRole());

        UserDtoResponse secondUser = result.getContent().get(1);
        assertEquals("user2@example.com", secondUser.getEmail());
        assertEquals("Bob", secondUser.getName());
        assertEquals("Johnson", secondUser.getLastName());
        assertEquals(Role.ADMIN, secondUser.getRole());

        verify(userRepository).findAll(any(PageRequest.class));
    }

    @Test
    void getAllUsers_EmptyPage_ShouldReturnEmptyPage() {
        Page<User> emptyPage = new PageImpl<>(List.of());
        when(userRepository.findAll(any(PageRequest.class))).thenReturn(emptyPage);

        PageDtoResponse<UserDtoResponse> result = userService.getAllUsers(0, 10);

        assertNotNull(result);
        assertEquals(0, result.getContent().size());
        assertEquals(0, result.getTotalElements());
        assertEquals(1, result.getTotalPages());
        assertEquals(0, result.getNumber());

        verify(userRepository).findAll(any(PageRequest.class));
    }

    @Test
    void deleteByEmail_UserExists_ShouldDeleteUser() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        doNothing().when(userRepository).delete(testUser);

        userService.deleteByEmail("test@example.com");

        verify(userRepository).findByEmail("test@example.com");
        verify(userRepository).delete(testUser);
    }

    @Test
    void deleteByEmail_UserNotFound_ShouldThrowException() {
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            userService.deleteByEmail("nonexistent@example.com");
        });

        verify(userRepository).findByEmail("nonexistent@example.com");
        verify(userRepository, never()).delete(any());
    }

    @Test
    void findUserByEmail_UserExists_ShouldReturnUser() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        UserDtoResponse result = userService.getUserByEmail("test@example.com");

        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
        assertEquals("John", result.getName());
        assertEquals("Doe", result.getLastName());

        verify(userRepository).findByEmail("test@example.com");
    }

    @Test
    void findUserByEmail_UserNotFound_ShouldThrowException() {
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            userService.getUserByEmail("nonexistent@example.com");
        });

        verify(userRepository).findByEmail("nonexistent@example.com");
    }
}