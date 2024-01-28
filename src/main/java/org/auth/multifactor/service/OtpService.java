package org.auth.multifactor.service;

import org.auth.multifactor.service.enumeration.OtpValidationStatus;

public interface OtpService {

    void create(String email);

    OtpValidationStatus validate(String email, String otp);

}
