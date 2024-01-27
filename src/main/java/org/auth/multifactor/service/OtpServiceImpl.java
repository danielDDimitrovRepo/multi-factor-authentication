package org.auth.multifactor.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.auth.multifactor.model.Otp;
import org.auth.multifactor.repository.OtpRepository;
import org.auth.multifactor.service.enumeration.Status;
import org.auth.multifactor.util.EncryptionUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Optional;

import static org.auth.multifactor.service.enumeration.Status.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class OtpServiceImpl implements OtpService {

    @Value("${application.otp-ttl-minutes}")
    private Long otpTtlMinutes;

    private final EncryptionUtil encryptionUtil;
    private final OtpRepository otpRepository;
    private final OutgoingMessageService emailSender;

    @Override
    public void createOtp(String email) {
        byte[] salt = encryptionUtil.generateSalt();
        String password = encryptionUtil.generateOneTimePassword();

        Otp otp = new Otp();
        otp.setEmail(email);
        otp.setOtp(encryptionUtil.generateHash(password, salt));
        otp.setSalt(salt);
        otp.setExpirationDateTime(LocalDateTime.now(ZoneOffset.UTC).plusMinutes(otpTtlMinutes));

        otpRepository.save(otp);
        emailSender.sendMessage(email, "Auth Code", password);
        log.info("Saved & sent OTP password for email: {}", email);

    }

    @Override
    public Status validate(String email, String otp) {
        Optional<Otp> dbResult = otpRepository.findTopByEmailOrderByExpirationDateTimeDesc(email);

        if (dbResult.isEmpty()) {
            return NOT_FOUND;
        }

        Otp result = dbResult.get();
        byte[] otpAsHash = encryptionUtil.generateHash(otp, result.getSalt());

        if (!Arrays.equals(result.getOtp(), otpAsHash)) {
            return INVALID;
        }

        if (result.getExpirationDateTime().isBefore(LocalDateTime.now(ZoneOffset.UTC))) {
            return EXPIRED;
        }

        return VALID;
    }

}
