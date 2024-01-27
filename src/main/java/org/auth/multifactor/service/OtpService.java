package org.auth.multifactor.service;

import org.auth.multifactor.model.Otp;
import org.auth.multifactor.service.enumeration.Status;

import java.util.Optional;

public interface OtpService {

    void createOtp(String email);

    Status validate(String email, String otp);

}
