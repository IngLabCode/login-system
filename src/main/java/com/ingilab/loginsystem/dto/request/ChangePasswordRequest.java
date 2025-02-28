package com.ingilab.loginsystem.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@Builder
public class ChangePasswordRequest {

    @NotBlank(message = "Current password cannot be blank")
    private String currentPassword;

    @NotBlank(message = "New password cannot be blank")
    @Size(min = 8, max = 20, message = "New password must be between 8 and 20 characters")
    private String newPassword;

    @NotBlank(message = "Confirmation password cannot be blank")
    private String confirmationPassword;
}