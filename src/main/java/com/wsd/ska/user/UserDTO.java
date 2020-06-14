package com.wsd.ska.user;

import com.wsd.ska.utils.FieldMatch;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldMatch.List({
        @FieldMatch(first = "password", second = "password_match", message = "Пароли не совпадают"),
})
public class UserDTO {

    @Pattern(regexp = "^[A-Za-z0-9]{5,20}$", flags = Pattern.Flag.UNICODE_CASE, message = "Имя пользователя может содержать только прописные и/или строчные латинские буквы, от 5 до 15 символов")
    private String username;

    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{4,15}$", flags = Pattern.Flag.UNICODE_CASE, message = "Пароль как минимум должен содержать: " +
            "одну цифру, " +
            "одну прописную латинскую букву, " +
            "одну заглавную латинскую букву, " +
            "один спец. символ( @ # $ % ^ & + = )." +
            "От 4 до 15 символов.")
    private String password;

    @NotEmpty(message = "Подтвердите пароль")
    private String password_match;

    @Email(message = "Ваша электронная почта не прошла проверку на допустимость")
    @NotEmpty(message = "Пожалуйста, предоставьте email")
    private String email;

    @Pattern(regexp = "^((\\+7|7|8)+([0-9]){10})$", flags = Pattern.Flag.UNICODE_CASE, message = "Ваш номер телефона не прошёл проверку, примеры: +79856214524, 79845145424, 89856145247")
    private String phone_number;

    @Pattern(regexp = "^[A-Za-zА-Яа-я]{1,45}$", message = "Максимальная длина имени 45 символов, допустима только кириллица и латиница")
    private String first_name;

    @Pattern(regexp = "^[A-Za-zА-Яа-я]{0,45}$", message = "Максимальная длина отчества 45 символов, допустима только кириллица и латиница")
    private String middle_name;

    @Pattern(regexp = "^[A-Za-zА-Яа-я]{1,45}$", message = "Максимальная длина фамилии 45 символов, допустима только кириллица и латиница")
    private String last_name;
}
