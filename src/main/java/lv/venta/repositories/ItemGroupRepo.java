package lv.venta.repositories;

import lv.venta.models.ItemGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.ArrayList;

public interface ItemGroupRepo extends JpaRepository<ItemGroup, Long> {

    @Query("SELECT g FROM ItemGroup g WHERE (:groupName is '' or g.groupName LIKE %:groupName%)")
    ArrayList<ItemGroup> findByFilter(@Param("groupName") String groupName);

    ItemGroup findByGroupName(String groupName);
}
