package org.auth.multifactor.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@Getter
@Setter
public class Otp {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String email;
    @Column(name = "one_time_password", columnDefinition = "BLOB")
    private byte[] otp;
    @Column(columnDefinition = "BLOB")
    private byte[] salt;
    @Column(columnDefinition = "TIMESTAMP")
    private LocalDateTime expirationDateTime;

}
