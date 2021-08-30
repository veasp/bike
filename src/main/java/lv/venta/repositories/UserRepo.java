package lv.venta.repositories;

import lv.venta.enums.UserType;
import lv.venta.models.RegisteredUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.ArrayList;

public interface UserRepo extends JpaRepository<RegisteredUser, Long> {

    boolean existsRegisteredUserByEmail(String email);
    RegisteredUser findByEmail(String email);

    @Query("SELECT u FROM RegisteredUser u WHERE (:name is '' or u.name LIKE %:name%) and (:surname is '' or u.surname LIKE %:surname%)"
            + " and (:email is '' or u.email LIKE %:email%) and (:phone is null or u.phone LIKE %:phone%) and (:type is null or u.type = :type)")
    ArrayList<RegisteredUser> findByFilter(@Param("name") String name, @Param("surname") String surname, @Param("email") String email, @Param("phone") String phone, @Param("type")UserType type);

    ArrayList<RegisteredUser> findByType(UserType userType);
}
