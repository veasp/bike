package lv.venta.models.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lv.venta.models.LanguageEntity;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter @Setter
@ToString
public class LanguageDto {

    @NotBlank
    private String key;

    private List<LanguageEntity> langEntities = new ArrayList<>();
}
