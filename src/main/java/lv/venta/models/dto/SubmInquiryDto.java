package lv.venta.models.dto;

import lombok.Getter;
import lombok.Setter;
import lv.venta.enums.InquiryStatus;

import javax.validation.constraints.Max;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Getter @Setter
public class SubmInquiryDto {

    private long inquiryId;

    @NotBlank
    @Size(max = 2048)
    private String adminNotes;
}
