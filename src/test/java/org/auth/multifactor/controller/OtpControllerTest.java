package org.auth.multifactor.controller;

import org.auth.multifactor.service.OtpService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.auth.multifactor.service.enumeration.OtpValidationStatus.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles(profiles = "test")
class OtpControllerTest {

    @MockBean
    private OtpService otpServiceImpl;

    @Captor
    private ArgumentCaptor<String> stringCaptor;

    @Autowired
    private MockMvc mockMvc;

    private String mockEmail = "junit@junit.com";
    private String mockPass = "123456";

    @Test
    public void create() throws Exception {
        MvcResult result = validatePostRequest("create", mockEmail, mockPass, status().isCreated());

        assertThat(result.getResponse().getContentAsString()).isNotBlank();

        verify(otpServiceImpl).create(stringCaptor.capture());
        assertThat(stringCaptor.getValue()).isEqualTo(mockEmail);
    }

    @Test
    public void create_InvalidEmail_ReturnBadRequest() throws Exception {
        MvcResult result = validatePostRequest("create", "invalidEmail", mockPass, status().isBadRequest());

        assertThat(result.getResponse().getContentAsString()).isNotBlank();
    }

    @Test
    public void validate() throws Exception {
        when(otpServiceImpl.validate(anyString(), anyString())).thenReturn(VALID);

        MvcResult result = validatePostRequest("validate", mockEmail, mockPass, status().isOk());

        assertThat(result.getResponse().getContentAsString()).isNotBlank();

        verify(otpServiceImpl).validate(stringCaptor.capture(), stringCaptor.capture());
        assertThat(stringCaptor.getAllValues()).containsExactlyElementsOf(asList(mockEmail, mockPass));
    }

    @Test
    public void validate_ServiceStatusNotFound_ReturnOk() throws Exception {
        when(otpServiceImpl.validate(anyString(), anyString())).thenReturn(NOT_FOUND);

        MvcResult result = validatePostRequest("validate", mockEmail, mockPass, status().isOk());

        assertThat(result.getResponse().getContentAsString()).isNotBlank();

        verify(otpServiceImpl).validate(stringCaptor.capture(), stringCaptor.capture());
        assertThat(stringCaptor.getAllValues()).containsExactlyElementsOf(asList(mockEmail, mockPass));
    }

    @Test
    public void validate_ServiceStatusUsed_ReturnUnprocessableEntity() throws Exception {
        when(otpServiceImpl.validate(anyString(), anyString())).thenReturn(USED);

        MvcResult result = validatePostRequest("validate", mockEmail, mockPass, status().isUnprocessableEntity());

        assertThat(result.getResponse().getContentAsString()).isNotBlank();

        verify(otpServiceImpl).validate(stringCaptor.capture(), stringCaptor.capture());
        assertThat(stringCaptor.getAllValues()).containsExactlyElementsOf(asList(mockEmail, mockPass));
    }

    @Test
    public void validate_ServiceStatusExpired_ReturnGone() throws Exception {
        when(otpServiceImpl.validate(anyString(), anyString())).thenReturn(EXPIRED);

        MvcResult result = validatePostRequest("validate", mockEmail, mockPass, status().isGone());

        assertThat(result.getResponse().getContentAsString()).isNotBlank();

        verify(otpServiceImpl).validate(stringCaptor.capture(), stringCaptor.capture());
        assertThat(stringCaptor.getAllValues()).containsExactlyElementsOf(asList(mockEmail, mockPass));
    }

    @Test
    public void validate_ServiceStatusInvalid_ReturnUnauthorized() throws Exception {
        when(otpServiceImpl.validate(anyString(), anyString())).thenReturn(INVALID);

        MvcResult result = validatePostRequest("validate", mockEmail, mockPass, status().isUnauthorized());

        assertThat(result.getResponse().getContentAsString()).isNotBlank();

        verify(otpServiceImpl).validate(stringCaptor.capture(), stringCaptor.capture());
        assertThat(stringCaptor.getAllValues()).containsExactlyElementsOf(asList(mockEmail, mockPass));
    }

    @Test
    public void validate_InvalidEmail_ReturnBadRequest() throws Exception {
        MvcResult result = validatePostRequest("validate", "invalidEmail", mockPass, status().isBadRequest());

        assertThat(result.getResponse().getContentAsString()).isNotBlank();
    }

    @Test
    public void validate_EmptyOtp_ReturnBadRequest() throws Exception {
        MvcResult result = validatePostRequest("validate", mockEmail, "", status().isBadRequest());

        assertThat(result.getResponse().getContentAsString()).isNotBlank();
    }

    private MvcResult validatePostRequest(String endpoint, String email, String otp, ResultMatcher httpStatusMatcher) throws Exception {
        return mockMvc.perform(MockMvcRequestBuilders
                        .post("/otp/" + endpoint)
                        .content(("email=" + email + "&otp=" + otp).getBytes())
                        .contentType(APPLICATION_FORM_URLENCODED_VALUE)
                        .accept(APPLICATION_FORM_URLENCODED_VALUE))
                .andExpect(httpStatusMatcher)
                .andReturn();
    }

}