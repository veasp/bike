package lv.venta.services;

import lv.venta.enums.InquiryStatus;
import lv.venta.models.CustomUserDetails;
import lv.venta.models.Inquiry;
import lv.venta.models.Item;
import lv.venta.models.RegisteredUser;
import lv.venta.models.dto.InquiryDto;
import lv.venta.models.dto.InquiryFilterDto;
import lv.venta.models.dto.InquiryItemDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;

public interface IInquiryService {

    /**
     * Returns all inquiries
     * @return All unfiltered inquiries
     */
    ArrayList<Inquiry> selectAllInquiries();

    /**
     * Returns inquiries by spring page object and optional filter
     * @param pageable Spring page
     * @param inquiryFilterDto Optional filter (can be null)
     * @return Page containing inquiries
     */
    Page<Inquiry> selectAllPageable(Pageable pageable, InquiryFilterDto inquiryFilterDto);

    /**
     * Returns all inquiries by user id
     * @param userId User id
     * @return All inquiries for user
     * @throws Exception If no user found
     */
    ArrayList<Inquiry> selectAllInquiriesByUser(long userId) throws Exception;

    /**
     * Adds new item to user inquiry
     * @param userDetails User details contains active inquiry
     * @param inquiryItemDto New inquiry item dto
     * @throws Exception If no item can be found by given parameters
     */
    void addNewInquiryItem(CustomUserDetails userDetails, InquiryItemDto inquiryItemDto) throws Exception;

    /**
     * Submits inquiry (reservation)
     * @param userDetails Contains active inquiry, will be reset
     * @param inquiryDto Submitted inquiry dto
     * @param price Calculated price
     * @throws Exception
     */
    void addNewInquiry(CustomUserDetails userDetails, InquiryDto inquiryDto, double price) throws Exception;

    /**
     * Returns inquiry by given id
     * @param id inquiry id
     * @return Inquiry
     * @throws Exception If no inquiry found
     */
    Inquiry selectById(long id) throws Exception;

    /**
     * Change inquiry status by given id and new status
     * @param id Inquiry id
     * @param newStatus New inquiry status
     * @throws Exception If no inquiry found
     */
    void changeInquiryStatusById(long id, InquiryStatus newStatus) throws Exception;

    /**
     * Edits inquiry admin notes
     * @param id Inquiry ID
     * @param newNotes New admin notes
     * @throws Exception If no inquiry found
     */
    void editInquiryNotesById(long id, String newNotes) throws Exception;

    /**
     * Deletes inquiry by given id
     * @param id Inquiry ID
     * @throws Exception If no inquiry found
     */
    void deleteInquiryById(long id) throws Exception;

    /**
     * Returns whether items are available at given inquiry dto (which contains dates)
     * @param inquiryDto Inquiry dto
     * @return Items are available
     */
    boolean isItemsAvailableAt(InquiryDto inquiryDto);
}
