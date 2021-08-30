package lv.venta.models.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

@Getter @Setter
@ToString
public class ItemGroupParamDto {

    @Size(max = 32)
    private String paramName;

    private List<String> paramValues = new ArrayList<>();

    private String newParamValue;
}