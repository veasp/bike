package lv.venta.models.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lv.venta.models.Inquiry;
import lv.venta.models.ItemGroup;
import lv.venta.models.Parameter;

import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter @Setter @ToString
public class InquiryItemDto {

    @NotNull
    private ItemGroup inqItemGroup;

    private Map<String, Parameter> paramValue = new HashMap<>();

    private Map<String, List<Parameter>> allParamValues = new HashMap<>();
}
