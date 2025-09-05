package com.example.bankcards.controller;

import com.example.bankcards.dto.page.PageDtoResponse;
import com.example.bankcards.dto.user.UserDtoResponse;
import com.example.bankcards.dto.user.UserEmailDtoRequest;
import com.example.bankcards.entity.user.Role;
import com.example.bankcards.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private UserEmailDtoRequest emailRequest;
    private UserDtoResponse userResponse;
    private PageDtoResponse<UserDtoResponse> pageResponse;

    @BeforeEach
    void setUp() {
        emailRequest = new UserEmailDtoRequest();
        emailRequest.setEmail("test@example.com");

        userResponse = UserDtoResponse.builder()
                .email("test@example.com")
                .name("John")
                .lastName("Doe")
                .role(Role.USER)
                .build();

        pageResponse = new PageDtoResponse<>(
                List.of(userResponse),
                1L,
                1,
                0
        );
    }

    @Test
    void getUserByEmail_ValidEmail_ShouldReturnUser() {
        when(userService.getUserByEmail(anyString())).thenReturn(userResponse);

        ResponseEntity<UserDtoResponse> response = userController.getUserByEmail(emailRequest);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals("test@example.com", response.getBody().getEmail());
        assertEquals("John", response.getBody().getName());
        assertEquals("Doe", response.getBody().getLastName());
        assertEquals(Role.USER, response.getBody().getRole());

        verify(userService).getUserByEmail(emailRequest.getEmail());
    }

    @Test
    void getAllUsers_ShouldReturnPageOfUsers() {
        when(userService.getAllUsers(anyInt(), anyInt())).thenReturn(pageResponse);

        ResponseEntity<PageDtoResponse<UserDtoResponse>> response = userController.getAllUsers(0, 10);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getContent().size());
        assertEquals(1L, response.getBody().getTotalElements());

        verify(userService).getAllUsers(0, 10);
    }

    @Test
    void getAllUsers_EmptyPage_ShouldReturnEmptyPage() {
        PageDtoResponse<UserDtoResponse> emptyPage = new PageDtoResponse<>(
                List.of(),
                0L,
                0,
                0
        );
        when(userService.getAllUsers(anyInt(), anyInt())).thenReturn(emptyPage);

        ResponseEntity<PageDtoResponse<UserDtoResponse>> response = userController.getAllUsers(0, 10);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(0, response.getBody().getContent().size());
        assertEquals(0L, response.getBody().getTotalElements());

        verify(userService).getAllUsers(0, 10);
    }

    @Test
    void deleteUser_ValidEmail_ShouldReturnSuccessMessage() {
        doNothing().when(userService).deleteByEmail(anyString());

        ResponseEntity<String> response = userController.deleteUser(emailRequest);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("The user has been successfully deleted", response.getBody());

        verify(userService).deleteByEmail(emailRequest.getEmail());
    }
}