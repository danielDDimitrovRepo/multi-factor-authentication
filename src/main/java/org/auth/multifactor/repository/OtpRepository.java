package org.auth.multifactor.repository;

import org.auth.multifactor.model.Otp;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OtpRepository extends MongoRepository<Otp, Long> {

    Optional<Otp> findTopByEmailOrderByExpirationDateTimeDesc(String email);

}