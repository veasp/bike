package lv.venta.models.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Min;
import javax.validation.constraints.Size;

@Getter @Setter
public class InquiryFilterDto {

    @Size(max = 19)
    private String userID;

    private String inquiryStatus;
}
