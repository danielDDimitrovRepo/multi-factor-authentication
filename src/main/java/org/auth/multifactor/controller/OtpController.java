package org.auth.multifactor.controller;


import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.auth.multifactor.service.OtpService;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/otp")
@RequiredArgsConstructor
@Validated
public class OtpController {

    private final OtpService otpEmailService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<String> create(@Valid @RequestParam @NotBlank @Email String email) {
        otpEmailService.createOtp(email);

        return ResponseEntity.ok(String.format("One Time Password sent to email: %s", email));
    }

    @GetMapping("/{email}/{otp}")
    public ResponseEntity<String> validate(@Valid @PathVariable @NotBlank @Email String email, @Valid @PathVariable @NotBlank String otp) {
        ResponseEntity<String> response;

        switch (otpEmailService.validate(email, otp)) {
            case NOT_FOUND -> response = ResponseEntity.ok(String.format("Cannot find user with email: %s", email));
            case EXPIRED -> response = ResponseEntity.status(HttpStatus.GONE).build(); // HTTP 410
            case INVALID -> response = ResponseEntity.status(HttpStatus.UNAUTHORIZED).build(); // HTTP 401
            case VALID -> response = ResponseEntity.ok(String.format("Code confirmed for user with email: %s", email));
            default -> response = ResponseEntity.noContent().build();
        }

        return response;

    }

}