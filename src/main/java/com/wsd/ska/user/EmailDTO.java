package com.wsd.ska.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EmailDTO {

    @Email(message = "Ваша электронная почта не прошла проверку на допустимость")
    @NotEmpty(message = "Пожалуйста, предоставьте email")
    private String email;
}
