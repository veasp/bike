package lv.venta.services;

import lv.venta.models.SettingEntity;
import lv.venta.models.dto.SettingsDto;

public interface ISettingsService {

    /**
     * Returns settings dto containing rules and email templates
     * @return Settings dto
     */
    SettingsDto getSettings();

    /**
     * Updates settings from settings dto
     * @param settingsDto Updated settings dto
     */
    void setSettings(SettingsDto settingsDto);

    /**
     * Returns setting entity by given key (rules, email template)
     * @param key Setting entity key
     * @return Setting entity
     */
    SettingEntity getByKey(String key);
}
