package org.auth.multifactor.controller;


import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.auth.multifactor.service.OtpService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static java.lang.String.format;

@Validated
@RestController
@RequestMapping("/otp")
@RequiredArgsConstructor
public class OtpController {

    private final OtpService otpServiceImpl;

    @PostMapping(path = "/create", consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE})
    public ResponseEntity<String> create(@Valid @RequestParam @NotBlank @Email String email) {
        otpServiceImpl.create(email);

        return ResponseEntity.status(HttpStatus.CREATED).body(format("One Time Password sent to email: %s", email));
    }

    @PostMapping(path = "/validate", consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE})
    public ResponseEntity<String> validate(@Valid @RequestParam @NotBlank @Email String email,
                                           @Valid @RequestParam @NotBlank String otp) {
        ResponseEntity<String> response;

        switch (otpServiceImpl.validate(email, otp)) {
            case NOT_FOUND -> response = ResponseEntity.ok(format("Cannot find user with email: %s", email));
            case USED ->
                    response = ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(format("Code was already used for email: %s", email)); // HTTP 422
            case EXPIRED ->
                    response = ResponseEntity.status(HttpStatus.GONE).body(format("Code has expired for email: %s", email)); // HTTP 410
            case INVALID ->
                    response = ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(format("Invalid code for email: %s", email)); // HTTP 401
            case VALID -> response = ResponseEntity.ok(format("Code confirmed for user with email: %s", email));
            default -> response = ResponseEntity.noContent().build();
        }

        return response;
    }

}