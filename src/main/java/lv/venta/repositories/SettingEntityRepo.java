package lv.venta.repositories;

import lv.venta.models.SettingEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.ArrayList;

public interface SettingEntityRepo extends JpaRepository<SettingEntity, Long> {

    SettingEntity findByKey(String key);
}