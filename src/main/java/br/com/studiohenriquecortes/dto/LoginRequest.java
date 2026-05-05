package br.com.studiohenriquecortes.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {

    @NotBlank(message = "O email e obrigatorio.")
    @Email(message = "O email informado e invalido.")
    private String email;

    @NotBlank(message = "A senha e obrigatoria.")
    private String password;
}
