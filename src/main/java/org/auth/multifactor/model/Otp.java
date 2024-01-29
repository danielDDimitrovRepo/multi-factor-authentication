package org.auth.multifactor.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.time.LocalDateTime;

@Document(collection = "one_time_password")
@Getter
@Setter
@NoArgsConstructor
public class Otp {

    @MongoId
    private String id;
    private String email;
    private byte[] otp;
    private byte[] salt;
    private LocalDateTime expirationDateTime;
    private boolean isUsed;

}
