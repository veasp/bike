package lv.venta.repositories;

import lv.venta.models.LanguageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;

@Repository
public interface LanguageRepo extends JpaRepository<LanguageEntity, Long> {

    ArrayList<LanguageEntity> findByKey(String key);
    LanguageEntity findByKeyAndLocale(String key, String locale);
    ArrayList<LanguageEntity> findByLocale(String locale);
}
