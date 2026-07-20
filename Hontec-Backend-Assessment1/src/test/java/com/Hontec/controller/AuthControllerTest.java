package com.Hontec.controller;

import com.Hontec.dto.LoginRequest;
import com.Hontec.dto.LoginResponse;
import com.Hontec.service.AuthService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    @Test
    void login_Success() {
        LoginRequest request = new LoginRequest("pharmacist@pharmacy.com", "password");
        LoginResponse response = new LoginResponse("mock-jwt-token", "pharmacist@pharmacy.com", "PHARMACIST");

        when(authService.login(request)).thenReturn(response);

        ResponseEntity<LoginResponse> responseEntity = authController.login(request);

        assertNotNull(responseEntity);
        assertEquals(200, responseEntity.getStatusCode().value());
        assertEquals(response, responseEntity.getBody());
    }

    @Test
    void register_Success() {
        com.Hontec.dto.RegisterRequest request = new com.Hontec.dto.RegisterRequest("new@pharmacy.com", "password", com.Hontec.model.Role.PHARMACIST);
        String responseMessage = "User registered successfully";

        when(authService.register(request)).thenReturn(responseMessage);

        ResponseEntity<String> responseEntity = authController.register(request);

        assertNotNull(responseEntity);
        assertEquals(200, responseEntity.getStatusCode().value());
        assertEquals(responseMessage, responseEntity.getBody());
    }
}
