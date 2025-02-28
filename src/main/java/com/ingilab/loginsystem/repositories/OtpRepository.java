package com.ingilab.loginsystem.repositories;

import com.ingilab.loginsystem.model.Otp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface OtpRepository extends JpaRepository<Otp, Integer> {

    @Query("SELECT o FROM Otp o WHERE o.otpCode = ?1 AND o.expiresAt > ?2 AND o.used = false")
    Optional<Otp> findValidOtp(String otpCode, LocalDateTime now);


    Optional<Otp> findTopByEmailOrderByCreatedAtDesc(String email);
}
