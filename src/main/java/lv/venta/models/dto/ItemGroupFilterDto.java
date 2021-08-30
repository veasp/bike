package lv.venta.models.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Size;

@Getter @Setter
public class ItemGroupFilterDto {

    @Size(max = 100)
    private String groupName;
}
