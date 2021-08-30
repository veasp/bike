package lv.venta.repositories;

import lv.venta.enums.ItemStatus;
import lv.venta.enums.UserType;
import lv.venta.models.Item;
import lv.venta.models.ItemGroup;
import lv.venta.models.RegisteredUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.ArrayList;

public interface ItemRepo extends JpaRepository<Item, Long> {

    @Query("SELECT i FROM Item i WHERE (:itemName is '' or i.itemName LIKE %:itemName%) and (:itemGroup is null or i.itemGroup = :itemGroup) and (:status is null or i.status = :status)")
    ArrayList<Item> findByFilter(@Param("itemName") String itemName, @Param("itemGroup") ItemGroup itemGroup, @Param("status") ItemStatus status);
}