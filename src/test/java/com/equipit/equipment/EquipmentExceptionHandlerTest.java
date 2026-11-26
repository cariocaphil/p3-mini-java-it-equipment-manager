package com.equipit.equipment;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class EquipmentExceptionHandlerTest {
    private static final String BORROW_BODY = "{\"firstName\":\"Ada\",\"lastName\":\"Lovelace\"}";

    @Mock
    private EquipmentService service;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new EquipmentController(service))
                .setControllerAdvice(new EquipmentExceptionHandler())
                .build();
    }

    @Test
    void borrow_whenEquipmentNotFound_returns404ProblemDetail() throws Exception {
        when(service.borrowEquipment(99L, "Ada", "Lovelace"))
                .thenThrow(new EquipmentNotFoundException(99L));

        mockMvc.perform(put("/api/equipment/99/borrow")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BORROW_BODY))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value("Equipment 99 not found"));
    }

    @Test
    void borrow_whenAlreadyBorrowed_returns409ProblemDetail() throws Exception {
        when(service.borrowEquipment(1L, "Ada", "Lovelace"))
                .thenThrow(new EquipmentStateException("Equipment 1 is already borrowed"));

        mockMvc.perform(put("/api/equipment/1/borrow")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BORROW_BODY))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.detail").value("Equipment 1 is already borrowed"));
    }

    @Test
    void returnEquipment_whenEquipmentNotFound_returns404ProblemDetail() throws Exception {
        when(service.returnEquipment(99L)).thenThrow(new EquipmentNotFoundException(99L));

        mockMvc.perform(put("/api/equipment/99/return"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void returnEquipment_whenNotBorrowed_returns409ProblemDetail() throws Exception {
        when(service.returnEquipment(1L))
                .thenThrow(new EquipmentStateException("Equipment 1 is not borrowed"));

        mockMvc.perform(put("/api/equipment/1/return"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.detail").value("Equipment 1 is not borrowed"));
    }
}
