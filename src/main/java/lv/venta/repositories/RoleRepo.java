package lv.venta.repositories;

import lv.venta.models.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepo extends JpaRepository<Role, Long> {

    Role getDistinctByName(String name);
}
