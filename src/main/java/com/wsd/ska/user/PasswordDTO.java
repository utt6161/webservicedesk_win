package com.wsd.ska.user;

import com.wsd.ska.utils.FieldMatch;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldMatch.List({
        @FieldMatch(first = "password", second = "password_match", message = "Пароли не совпадают"),
})
public class PasswordDTO {

    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{4,15}$", flags = Pattern.Flag.UNICODE_CASE, message = "Пароль как минимум должен содержать: " +
            "одну цифру, " +
            "одну прописную латинскую букву, " +
            "одну заглавную латинскую букву, " +
            "один спец. символ( @ # $ % ^ & + = )." +
            "От 4 до 15 символов.")
    private String password;

    @NotEmpty(message = "Подтвердите пароль")
    private String password_match;
}
