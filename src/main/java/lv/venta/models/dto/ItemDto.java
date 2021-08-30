package lv.venta.models.dto;

import lombok.Getter;
import lombok.Setter;
import lv.venta.enums.ItemStatus;
import lv.venta.models.ItemGroup;
import lv.venta.models.Parameter;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter @Setter
public class ItemDto {

    @NotNull
    private ItemGroup itemGroup;

    @NotBlank
    @Size(max = 128)
    private String itemName;

    @Min(0)
    @Max(1000000)
    private int quantity;

    private ItemStatus status = ItemStatus.AVAILABLE;

    private MultipartFile image;
    private String imageSrc;

    private long itemId;

    private Map<String, Parameter> paramValue = new HashMap<>();
    private Map<String, List<Parameter>> allParamValues = new HashMap<>();
}
