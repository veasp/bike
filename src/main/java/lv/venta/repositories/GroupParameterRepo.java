package lv.venta.repositories;

import lv.venta.models.GroupParameter;
import lv.venta.models.ItemGroup;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupParameterRepo extends JpaRepository<GroupParameter, Long> {

    GroupParameter findByItemGroupAndParamName(ItemGroup itemGroup, String key);
}
