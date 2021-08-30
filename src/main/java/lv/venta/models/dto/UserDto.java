package lv.venta.models.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lv.venta.validations.IValidation;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Getter @Setter
@ToString
public class UserDto {

    @NotBlank
    @Size(max = 35)
    private String firstName;

    @NotBlank
    @Size(max = 35)
    private String lastName;

    @NotBlank
    @Size(min = 5, max = 64)
    private String password;
    @NotBlank
    private String matchingPassword;

    @NotBlank
    @IValidation.ValidEmail
    private String email;
}