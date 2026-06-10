package ru.mentee.power.crm.spring.exception;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.mentee.power.crm.service.LeadService;
import ru.mentee.power.crm.spring.mapper.LeadMapper;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class GlobalExceptionHandlerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private LeadService leadService;

  @MockitoBean private LeadMapper leadMapper;

  @Test
  void shouldReturn404_whenEntityNotFound() throws Exception {
    UUID nonExistentId = UUID.randomUUID();

    when(leadService.getLeadById(any(UUID.class)))
        .thenThrow(new EntityNotFoundException("Lead", nonExistentId.toString()));

    mockMvc
        .perform(get("/api/leads/{id}", nonExistentId))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.timestamp").exists())
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.error").value("Not Found"))
        .andExpect(jsonPath("$.message").value("Lead not found with id: " + nonExistentId))
        .andExpect(jsonPath("$.path").value("/api/leads/" + nonExistentId))
        .andExpect(jsonPath("$.errors").doesNotExist());
  }

  @Test
  void shouldReturn400WithFieldErrors_whenValidationFails() throws Exception {
    String invalidJson =
        """
            {
                "email": "",
                "firstName": "",
                "phone": "+123"
            }
            """;

    mockMvc
        .perform(post("/api/leads").contentType(MediaType.APPLICATION_JSON).content(invalidJson))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.timestamp").exists())
        .andExpect(jsonPath("$.status").value(400))
        .andExpect(jsonPath("$.error").value("Bad Request"))
        .andExpect(jsonPath("$.message").value("Validation failed"))
        .andExpect(jsonPath("$.path").value("/api/leads"))
        .andExpect(jsonPath("$.errors").isMap())
        .andExpect(jsonPath("$.errors").isNotEmpty());
  }

  @Test
  void shouldReturn500_whenUnexpectedExceptionOccurs() throws Exception {
    UUID someId = UUID.randomUUID();
    when(leadService.getLeadById(any(UUID.class)))
        .thenThrow(new RuntimeException("Database connection failed"));

    mockMvc
        .perform(get("/api/leads/{id}", someId))
        .andExpect(status().isInternalServerError())
        .andExpect(jsonPath("$.timestamp").exists())
        .andExpect(jsonPath("$.status").value(500))
        .andExpect(jsonPath("$.error").value("Internal Server Error"))
        .andExpect(
            jsonPath("$.message").value("Internal server error occurred. Please contact support."))
        .andExpect(jsonPath("$.path").value("/api/leads/" + someId))
        .andExpect(jsonPath("$.errors").doesNotExist())
        .andExpect(
            jsonPath("$.message")
                .value(
                    org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.containsString("Database connection failed"))));
  }
}
