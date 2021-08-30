package lv.venta.models.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class SettingsDto {

    private String rules;
    private String aboutUs;

    private String emailAccepted;
    private String emailRejected;
    private String emailNewInquiry;
}
