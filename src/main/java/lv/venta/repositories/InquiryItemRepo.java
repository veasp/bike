package lv.venta.repositories;

import lv.venta.models.InquiryItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InquiryItemRepo extends JpaRepository<InquiryItem, Long> {
}
