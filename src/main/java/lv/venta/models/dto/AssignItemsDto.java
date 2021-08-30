package lv.venta.models.dto;

import lombok.Getter;
import lombok.Setter;
import lv.venta.models.AssignedInquiryItem;
import lv.venta.models.Item;

import java.util.ArrayList;

@Getter @Setter
public class AssignItemsDto {

    private long inquiryId;
    private ArrayList<AssignedInquiryItem> assignedItems = new ArrayList<>();

    private Item newAssign;
}
