package lv.venta.models.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

@Getter @Setter
@ToString
public class ItemGroupDto {

    @NotBlank
    @Size(max = 128)
    private String groupName;

    @Size(max = 1024)
    private String groupDesc;

    @Min(0)
    private double price;

    @NotBlank
    @Size(max = 64)
    private String newInputParamName;

    private List<ItemGroupParamDto> parameters = new ArrayList<>();

    private long groupId;

    private MultipartFile image;
    private String imageSrc;
}