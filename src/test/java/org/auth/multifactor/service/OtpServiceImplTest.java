package org.auth.multifactor.service;

import org.auth.multifactor.model.Otp;
import org.auth.multifactor.repository.OtpRepository;
import org.auth.multifactor.service.enumeration.OtpValidationStatus;
import org.auth.multifactor.util.EncryptionUtil;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Optional;

import static java.time.ZoneOffset.UTC;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.auth.multifactor.service.enumeration.OtpValidationStatus.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles(profiles = "test")
class OtpServiceImplTest {

    @MockBean
    private OtpRepository otpRepository;
    @MockBean
    private OutgoingMessageService emailSender;
    @SpyBean
    private EncryptionUtil encryptionUtil;

    @Captor
    private ArgumentCaptor<String> stringCaptor;
    @Captor
    private ArgumentCaptor<Otp> otpCaptor;
    @Captor
    private ArgumentCaptor<byte[]> byteCaptor;

    @Autowired
    private OtpServiceImpl otpServiceImpl;

    private EncryptionUtil testEncryptionUtil = new EncryptionUtil();
    private String mockEmail = "junit@junit.com";
    private String mockPass = "123456";

    @Test
    void create() {
        when(encryptionUtil.generateOneTimePassword()).thenReturn(mockPass);

        otpServiceImpl.create(mockEmail);

        verify(emailSender).sendMessage(stringCaptor.capture(), stringCaptor.capture(), stringCaptor.capture());
        assertThat(stringCaptor.getAllValues()).containsExactlyElementsOf(asList(mockEmail, "Auth Code", mockPass));

        verify(otpRepository).save(otpCaptor.capture());

        Otp actualOtp = otpCaptor.getValue();
        assertThat(actualOtp.getEmail()).isEqualTo(mockEmail);
        assertThat(actualOtp.getOtp()).isExactlyInstanceOf(byte[].class);
        assertThat(actualOtp.getSalt()).isExactlyInstanceOf(byte[].class);
        assertThat(actualOtp.getExpirationDateTime()).isAfter(LocalDateTime.now(UTC));
        assertThat(actualOtp.isUsed()).isFalse();
    }

    @Test
    void validate() {
        Otp mockOtp = buildMockOtp();

        when(encryptionUtil.generateOneTimePassword()).thenReturn(mockPass);
        when(otpRepository.findTopByEmailOrderByExpirationDateTimeDesc(anyString())).thenReturn(Optional.of(mockOtp));

        OtpValidationStatus actualStatus = otpServiceImpl.validate(mockEmail, mockPass);

        assertThat(actualStatus).isEqualTo(VALID);

        verify(otpRepository).findTopByEmailOrderByExpirationDateTimeDesc(stringCaptor.capture());
        assertThat(stringCaptor.getValue()).isEqualTo(mockEmail);

        verify(encryptionUtil).generateHash(stringCaptor.capture(), byteCaptor.capture());
        assertThat(stringCaptor.getValue()).isEqualTo(mockPass);
        assertThat(byteCaptor.getValue()).isEqualTo(mockOtp.getSalt());

        verify(otpRepository).save(otpCaptor.capture());
        assertThat(otpCaptor.getValue())
                .usingRecursiveComparison()
                .isEqualTo(mockOtp);

    }

    @Test
    void validate_OtpNotFoundInDatabase_NotFoundReturned() {
        when(otpRepository.findTopByEmailOrderByExpirationDateTimeDesc(anyString())).thenReturn(Optional.empty());

        OtpValidationStatus actualStatus = otpServiceImpl.validate(mockEmail, mockPass);

        assertThat(actualStatus).isEqualTo(NOT_FOUND);
    }

    @Test
    void validate_DatabaseOtpIsInvalid_InvalidReturned() {
        Otp mockOtp = buildMockOtp();
        mockOtp.setOtp(testEncryptionUtil.generateHash("invalid", mockOtp.getSalt()));

        when(otpRepository.findTopByEmailOrderByExpirationDateTimeDesc(anyString())).thenReturn(Optional.of(mockOtp));

        OtpValidationStatus actualStatus = otpServiceImpl.validate(mockEmail, mockPass);

        assertThat(actualStatus).isEqualTo(INVALID);
    }

    @Test
    void validate_DatabaseOtpIsUsed_UsedReturned() {
        Otp mockOtp = buildMockOtp();
        mockOtp.setUsed(true);

        when(otpRepository.findTopByEmailOrderByExpirationDateTimeDesc(anyString())).thenReturn(Optional.of(mockOtp));

        OtpValidationStatus actualStatus = otpServiceImpl.validate(mockEmail, mockPass);

        assertThat(actualStatus).isEqualTo(USED);
    }

    @Test
    void validate_DatabaseOtpIsExpired_ExpiredReturned() {
        Otp mockOtp = buildMockOtp();
        mockOtp.setExpirationDateTime(LocalDateTime.now(UTC).minusMinutes(1));

        when(otpRepository.findTopByEmailOrderByExpirationDateTimeDesc(anyString())).thenReturn(Optional.of(mockOtp));

        OtpValidationStatus actualStatus = otpServiceImpl.validate(mockEmail, mockPass);

        assertThat(actualStatus).isEqualTo(EXPIRED);
    }


    private Otp buildMockOtp() {
        Otp mockOtp = new Otp();
        mockOtp.setId(1L);
        mockOtp.setEmail(mockEmail);
        mockOtp.setSalt(testEncryptionUtil.generateSalt());
        mockOtp.setOtp(testEncryptionUtil.generateHash(mockPass, mockOtp.getSalt()));
        mockOtp.setExpirationDateTime(LocalDateTime.now(UTC).plusMinutes(3));
        mockOtp.setUsed(false);
        return mockOtp;
    }

}