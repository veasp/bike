package lv.venta.services;

import lv.venta.models.GroupParameter;
import lv.venta.models.ItemGroup;
import lv.venta.models.RegisteredUser;
import lv.venta.models.dto.ItemGroupDto;
import lv.venta.models.dto.ItemGroupFilterDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;

public interface IItemGroupService {

    /**
     * Returns item groups by spring page object and optional filter
     * @param pageable Spring page
     * @param itemGroupFilterDto Optional filter (can be null)
     * @return Page containing item groups
     */
    Page<ItemGroup> selectAllPageable(Pageable pageable, ItemGroupFilterDto itemGroupFilterDto);

    /**
     * Returns all item groups
     * @return All item groups
     */
    ArrayList<ItemGroup> selectAllItemGroups();

    /**
     * Adds new item group by given dto
     * @param itemGroupDto Item group dto
     * @throws Exception If image could not be uploaded
     */
    void addNewItemGroup(ItemGroupDto itemGroupDto) throws Exception;

    /**
     * Returns item group by id
     * @param id Item group id
     * @return Item group
     * @throws Exception If no item group found
     */
    ItemGroup selectById(long id) throws Exception;

    /**
     * Updates item group by given id and dto
     * @param id Item group id
     * @param itemGroupDto Dto with changes
     * @throws Exception If no item group found or image could not be uploaded
     */
    void updateItemGroupById(long id, ItemGroupDto itemGroupDto) throws Exception;

    /**
     * Deletes item group by given id
     * @param id Item group id
     * @throws Exception If no item group found
     */
    void deleteItemGroupById(long id) throws Exception;
}
