package br.com.studiohenriquecortes.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequest {

    @NotBlank(message = "O nome e obrigatorio.")
    @Size(max = 120, message = "O nome deve ter no maximo 120 caracteres.")
    private String name;

    @NotBlank(message = "O email e obrigatorio.")
    @Email(message = "O email informado e invalido.")
    @Size(max = 150, message = "O email deve ter no maximo 150 caracteres.")
    private String email;

    @NotBlank(message = "A senha e obrigatoria.")
    @Size(min = 6, max = 100, message = "A senha deve ter entre 6 e 100 caracteres.")
    private String password;

    @Size(max = 20, message = "O telefone deve ter no maximo 20 caracteres.")
    @Pattern(
            regexp = "^$|^(\\+?\\d[\\d\\s()\\-]{9,19})$",
            message = "O telefone informado e invalido."
    )
    private String phone;
}
