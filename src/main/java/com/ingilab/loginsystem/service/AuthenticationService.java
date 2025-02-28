package com.ingilab.loginsystem.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ingilab.loginsystem.dto.request.AuthenticationRequest;
import com.ingilab.loginsystem.dto.request.ForgotPasswordRequest;
import com.ingilab.loginsystem.dto.request.VerifyOtpRequest;
import com.ingilab.loginsystem.dto.response.AuthenticationResponse;
import com.ingilab.loginsystem.dto.request.RegisterRequest;
import com.ingilab.loginsystem.dto.response.ForgotPasswordResponse;
import com.ingilab.loginsystem.dto.response.ResetPasswordResponse;
import com.ingilab.loginsystem.model.Otp;
import com.ingilab.loginsystem.repositories.OtpRepository;
import com.ingilab.loginsystem.token.Token;
import com.ingilab.loginsystem.repositories.TokenRepository;
import com.ingilab.loginsystem.enums.TokenType;
import com.ingilab.loginsystem.enums.Role;
import com.ingilab.loginsystem.model.User;
import com.ingilab.loginsystem.repositories.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserRepository repository;
    private final TokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final OtpRepository otpRepository;
    private final EmailService emailService;

    public AuthenticationResponse register(RegisterRequest registerRequest) {
        var user = User.builder()
                .firstname(registerRequest.getFirstname())
                .lastname(registerRequest.getLastname())
                .email(registerRequest.getEmail())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .role(Role.USER)
                .build();
        var savedUser = repository.save(user);
        var jwtToken = jwtService.generateToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);
        saveUserToken(savedUser, jwtToken);
        return AuthenticationResponse.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken)
                .build();
    }

    public AuthenticationResponse authenticate(AuthenticationRequest authenticationRequest) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        authenticationRequest.getEmail(),
                        authenticationRequest.getPassword()
                )
        );
        var user = repository.findByEmail(authenticationRequest.getEmail())
                .orElseThrow();
        var jwtToken = jwtService.generateToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);
        revokeAllUserTokens(user);
        saveUserToken(user, jwtToken);
        return AuthenticationResponse.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken)
                .build();
    }

    private void saveUserToken(User user, String jwtToken) {
        var token = Token.builder()
                .user(user)
                .token(jwtToken)
                .tokenType(TokenType.BEARER)
                .expired(false)
                .revoked(false)
                .build();
        tokenRepository.save(token);
    }

    private void revokeAllUserTokens(User user) {
        var validUserTokens = tokenRepository.findAllValidTokenByUser(user.getId());
        if (validUserTokens.isEmpty())
            return;
        validUserTokens.forEach(token -> {
            token.setExpired(true);
            token.setRevoked(true);
        });
        tokenRepository.saveAll(validUserTokens);
    }

    public void refreshToken(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {
        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        final String refreshToken;
        final String userEmail;
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return;
        }
        refreshToken = authHeader.substring(7);
        userEmail = jwtService.extractUsername(refreshToken);
        if (userEmail != null) {
            var user = this.repository.findByEmail(userEmail)
                    .orElseThrow();
            if (jwtService.isTokenValid(refreshToken, user)) {
                var accessToken = jwtService.generateToken(user);
                revokeAllUserTokens(user);
                saveUserToken(user, accessToken);
                var authResponse = AuthenticationResponse.builder()
                        .accessToken(accessToken)
                        .refreshToken(refreshToken)
                        .build();
                new ObjectMapper().writeValue(response.getOutputStream(), authResponse);
            }
        }
    }

    public ForgotPasswordResponse forgotPassword(ForgotPasswordRequest forgotPasswordRequest) {
        var userOptional = repository.findByEmail(forgotPasswordRequest.getEmail());

        if (userOptional.isEmpty()) {
            return ForgotPasswordResponse.builder()
                    .message("If your email exists in our system, an OTP has been sent.")
                    .success(false)
                    .build();
        }

        String otp = generateOtp();

        Otp otpEntity = Otp.builder()
                .email(forgotPasswordRequest.getEmail())
                .otpCode(otp)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .used(false)
                .build();

        otpRepository.save(otpEntity);

        emailService.sendOtpEmail(forgotPasswordRequest.getEmail(), otp);

        return ForgotPasswordResponse.builder()
                .message("OTP sent to your email.")
                .success(true)
                .build();
    }

    public ResetPasswordResponse verifyOtpAndResetPassword(VerifyOtpRequest verifyOtpRequest) {
        if (!verifyOtpRequest.getNewPassword().equals(verifyOtpRequest.getConfirmPassword())) {
            return ResetPasswordResponse.builder()
                    .message("New password and confirm password do not match.")
                    .success(false)
                    .build();
        }

        var validOtp = otpRepository.findValidOtp(
                verifyOtpRequest.getOtp(),
                LocalDateTime.now()
        );

        if (validOtp.isEmpty()) {
            return ResetPasswordResponse.builder()
                    .message("Invalid or expired OTP.")
                    .success(false)
                    .build();
        }

        Otp otp = validOtp.get();
        otp.setUsed(true);
        otpRepository.save(otp);

        String email = otp.getEmail();

        var user = repository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setPassword(passwordEncoder.encode(verifyOtpRequest.getNewPassword()));
        repository.save(user);

        return ResetPasswordResponse.builder()
                .message("Password reset successfully.")
                .success(true)
                .build();
    }


    private String generateOtp() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }
}
