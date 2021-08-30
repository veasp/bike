package lv.venta.repositories;

import lv.venta.enums.InquiryStatus;
import lv.venta.enums.UserType;
import lv.venta.models.Inquiry;
import lv.venta.models.RegisteredUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.ArrayList;

public interface InquiryRepo extends JpaRepository<Inquiry, Long> {

    @Query("SELECT i FROM Inquiry i WHERE (:user is null or i.inquiryUser = :user) and (:inquiryStatus is null or i.inquiryStatus = :inquiryStatus) ORDER BY i.inqId DESC")
    ArrayList<Inquiry> findByFilter(@Param("user") RegisteredUser user, @Param("inquiryStatus") InquiryStatus inquiryStatus);
    ArrayList<Inquiry> findByOrderByInqIdDesc();

    ArrayList<Inquiry> findByInquiryUser(RegisteredUser user);
    ArrayList<Inquiry> findByInquiryStatus(InquiryStatus status);
}
