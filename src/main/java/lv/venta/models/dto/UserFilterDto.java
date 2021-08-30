package lv.venta.models.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.Size;

@Getter @Setter
public class UserFilterDto {

    @Size(max = 35)
    private String name;

    @Size(max = 35)
    private String surname;

    @Size(max = 320)
    private String email;

    @Size(max = 16)
    private String phone;

    private String userType;
}
