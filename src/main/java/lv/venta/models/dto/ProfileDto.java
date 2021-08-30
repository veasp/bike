package lv.venta.models.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lv.venta.validations.IValidation;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Getter @Setter
@ToString
@NoArgsConstructor
public class ProfileDto {

    @NotBlank
    @Size(max = 35)
    private String firstName;

    @NotBlank
    @Size(max = 35)
    private String lastName;

    @NotBlank
    @IValidation.ValidEmail
    private String email;

    @Size(max = 16)
    private String phone;

    public ProfileDto(@NotBlank @Size(max = 35) String firstName, @NotBlank @Size(max = 35) String lastName, @NotBlank String email, @Size(max = 16) String phone) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
    }
}