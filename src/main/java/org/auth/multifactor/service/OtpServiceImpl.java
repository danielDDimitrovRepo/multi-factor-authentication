package org.auth.multifactor.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.auth.multifactor.model.Otp;
import org.auth.multifactor.repository.OtpRepository;
import org.auth.multifactor.service.enumeration.OtpValidationStatus;
import org.auth.multifactor.util.PasswordUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import static java.time.ZoneOffset.UTC;
import static org.auth.multifactor.service.enumeration.OtpValidationStatus.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class OtpServiceImpl implements OtpService {

    @Value("${application.otp-ttl-minutes}")
    private Long otpTtlMinutes;

    private final PasswordUtil passwordUtil;
    private final OtpRepository otpRepository;
    private final OutgoingMessageService outgoingEmailMessageService;

    @Override
    public void create(String email) {
        byte[] salt = passwordUtil.generateSalt();
        String password = passwordUtil.generateOneTimePassword();

        Otp otp = new Otp();
        otp.setEmail(email);
        otp.setOtp(passwordUtil.generateHash(password, salt));
        otp.setSalt(salt);
        otp.setExpirationDateTime(LocalDateTime.now(UTC).plusMinutes(otpTtlMinutes));

        otpRepository.save(otp);
        outgoingEmailMessageService.sendMessage(email, "Auth Code", password);
        log.info("Saved & sent OTP password for email: {}", email);

    }

    @Override
    public OtpValidationStatus validate(String email, String otp) {
        Optional<Otp> dbResult = otpRepository.findTopByEmailOrderByExpirationDateTimeDesc(email);

        if (dbResult.isEmpty()) {
            log.info("Not Found OTP password for email: {}", email);
            return NOT_FOUND;
        }

        Otp result = dbResult.get();
        byte[] otpAsHash = passwordUtil.generateHash(otp, result.getSalt());

        if (!Arrays.equals(result.getOtp(), otpAsHash)) {
            log.info("Invalid OTP password for email: {}", email);
            return INVALID;
        }

        if (result.isUsed()) {
            log.info("Used OTP password for email: {}", email);
            return USED;
        } else if (result.getExpirationDateTime().isBefore(LocalDateTime.now(UTC))) {
            log.info("Expired OTP password for email: {}", email);
            return EXPIRED;
        }

        result.setUsed(true);
        otpRepository.save(result);
        log.info("Validated OTP password for email: {}", email);

        return VALID;
    }

}
