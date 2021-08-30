package lv.venta.repositories;

import lv.venta.models.AssignedInquiryItem;
import lv.venta.models.Inquiry;
import lv.venta.models.Item;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.ArrayList;

public interface AssignedInquiryItemRepo extends JpaRepository<AssignedInquiryItem, Long> {

    ArrayList<AssignedInquiryItem> findByAssignItemAndAssignInquiry(Item item, Inquiry inquiry);
}