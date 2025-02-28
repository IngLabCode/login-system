package com.ingilab.loginsystem.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "Firstname cannot be blank")
    @Size(min = 2, max = 50, message = "Firstname must be between 2 and 50 characters")
    private String firstname;

    @NotBlank(message = "Lastname cannot be blank")
    @Size(min = 2, max = 50, message = "Lastname must be between 2 and 50 characters")
    private String lastname;

    @NotBlank(message = "Phone number cannot be blank")
    @Pattern(
            regexp = "^\\+?[1-9]\\d{1,14}$",
            message = "Phone number must be a valid international number (e.g., +1234567890)"
    )
    private String phone;


    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Email should be valid")
    @Pattern(
            regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$",
            message = "Email should follow the standard pattern (e.g., example@mail.com)"
    )
    private String email;


    @NotBlank(message = "Password cannot be blank")
    @Size(min = 8, max = 20, message = "Password must be between 8 and 20 characters")
    private String password;
}