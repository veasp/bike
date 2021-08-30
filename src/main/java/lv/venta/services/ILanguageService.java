package lv.venta.services;

import lv.venta.models.LanguageEntity;
import lv.venta.models.dto.LanguageDto;

import java.util.ArrayList;

public interface ILanguageService {

    /**
     * Adds new locale key for given list of languages
     * @param key Locale key
     * @param languages List of languages
     * @throws Exception Key already exists
     */
    void addNewLangKey(String key, ArrayList<String> languages) throws Exception;

    /**
     * Returns language entity by given key and locale.
     * If entity is not found, it will be created and saved in db.
     * @param key Key
     * @param locale Locale
     * @return Language entity
     */
    LanguageEntity findByKeyAndLocale(String key, String locale);

    /**
     * Returns all language entities
     * @return All language entities
     */
    ArrayList<LanguageEntity> selectAllEntities();

    /**
     * Saves given language entity
     * @param languageEntity Language entity
     * @throws Exception Content field is null or empty
     */
    void saveEntity(LanguageEntity languageEntity) throws Exception;

    /**
     * Deletes language entity by key
     * @param key Language entity key
     * @throws Exception No entity could be found
     */
    void deleteByKey(String key) throws Exception;

    /**
     * Pulls locale data from db and stores (caches) it on memory.
     */
    void reloadFromDb();

    /**
     * Returns list of currently hard-coded languages
     * @return List of registered languages
     */
    ArrayList<String> getLanguages();
}
