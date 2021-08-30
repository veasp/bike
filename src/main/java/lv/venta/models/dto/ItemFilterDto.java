package lv.venta.models.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Size;

@Getter @Setter
public class ItemFilterDto {

    @Size(max = 100)
    private String itemName;

    private String itemGroup;

    private String status;
}
