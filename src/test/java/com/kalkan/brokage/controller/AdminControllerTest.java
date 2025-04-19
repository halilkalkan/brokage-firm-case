package com.kalkan.brokage.controller;

import com.kalkan.brokage.service.AdminService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration
class AdminControllerTest {

    @Mock
    private AdminService adminService;

    @InjectMocks
    private AdminController adminController;

    @Test
    @WithMockUser(roles = "ADMIN")
    void matchOrder_AdminUser_SuccessfulMatch_ReturnsSuccessMessage() {
        Long orderId = 1L;
        String successMessage = "Order successfully matched";
        when(adminService.matchOrder(orderId)).thenReturn(successMessage);

        ResponseEntity<?> response = adminController.matchOrder(orderId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(successMessage, response.getBody());
        verify(adminService).matchOrder(orderId);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void matchOrder_AdminUser_IllegalState_ReturnsBadRequest() {
        Long orderId = 1L;
        String errorMessage = "Order cannot be matched";
        when(adminService.matchOrder(orderId)).thenThrow(new IllegalStateException(errorMessage));

        ResponseEntity<?> response = adminController.matchOrder(orderId);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(errorMessage, response.getBody());
        verify(adminService).matchOrder(orderId);
    }
}
