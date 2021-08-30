package lv.venta.models.dto;

import lombok.Getter;
import lombok.Setter;
import lv.venta.models.ItemGroup;

@Getter @Setter
public class FilterDto {

    private ItemGroup itemGroup;
}
