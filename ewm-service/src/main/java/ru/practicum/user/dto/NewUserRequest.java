package ru.practicum.user.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Getter
@Setter
public class NewUserRequest {
    @NotBlank(message = "Имя не может быть пустым")
    @Size(min = 2, max = 250)
    private String name;

    @NotBlank(message = "Email не может быть пустым")
    @Email(message = "Некорректный email")
    @Size(min = 6, max = 254)
    private String email;
}