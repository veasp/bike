package lv.venta.services;

import lv.venta.models.Inquiry;
import lv.venta.models.Item;
import lv.venta.models.dto.ItemDto;
import lv.venta.models.dto.ItemFilterDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.ArrayList;

public interface IItemService {

    /**
     * Returns all items
     * @return All items
     */
    ArrayList<Item> selectAllItems();

    /**
     * Returns items by spring page object and optional filter
     * @param pageable Spring page
     * @param itemFilterDto Optional filter (can be null)
     * @return Page containing items
     */
    Page<Item> selectAllPageable(Pageable pageable, ItemFilterDto itemFilterDto);

    /**
     * Adds new item by given dto
     * @param itemDto Item dto
     * @throws Exception If image could not be uploaded
     */
    void addNewItem(ItemDto itemDto) throws Exception;

    /**
     * Updates item by given id and dto
     * @param id Item id
     * @param itemDto Changed item dto
     * @throws Exception If no item could be found or image could not be uploaded
     */
    void updateItemById(long id, ItemDto itemDto) throws Exception;

    /**
     * Returns item by id
     * @param id Item id
     * @return Item
     * @throws Exception If no item could be found
     */
    Item selectById(long id) throws Exception;

    /**
     * Deletes item by id
     * @param id Item id
     * @throws Exception If no item could be found
     */
    void deleteItemById(long id) throws Exception;

    /**
     * Adds item to inquiry (assigns it)
     * @param item Item to assign
     * @param inquiry Inquiry
     * @throws Exception If no inquiry provided or item is not available in given inquiry dates
     */
    void addNewAssignedInq(Item item, Inquiry inquiry) throws Exception;

    /**
     * Removes assigned item from its inquiry by assignment id
     * @param id Assignment id
     * @throws Exception If no assigned inquiry item could be found
     */
    void removeAssignedInq(long id) throws Exception;

    /**
     * Returns all available items to given inquiry (by date and count).
     * Sorted by matching parameters first.
     * @param inquiry Inquiry
     * @return List of available items
     */
    ArrayList<Item> selectAllFilteredByInquiry(Inquiry inquiry);

    /**
     * Returns count of item quantity availability between given dates
     * @param item Item
     * @param dateFrom Date from
     * @param dateTo Date to
     * @return Count of items available
     */
    int getAvailableCountAt(Item item, LocalDateTime dateFrom, LocalDateTime dateTo);
}
