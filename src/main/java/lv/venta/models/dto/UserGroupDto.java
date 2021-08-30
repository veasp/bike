package lv.venta.models.dto;

import lombok.Getter;
import lombok.Setter;
import lv.venta.enums.UserType;

import javax.validation.constraints.NotNull;

@Getter @Setter
public class UserGroupDto {

    private long id;

    @NotNull
    private UserType type;

    public UserGroupDto(long id, UserType type) {
        this.id = id;
        this.type = type;
    }
}
