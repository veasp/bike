package lv.venta.services.impl;

import lv.venta.models.LanguageEntity;
import lv.venta.models.dto.LanguageDto;
import lv.venta.repositories.LanguageRepo;
import lv.venta.services.ILanguageService;
import lv.venta.utils.MessageLocale;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class LanguageService implements ILanguageService {

    @Autowired
    private MessageLocale messageLocale;

    @Autowired
    LanguageRepo languageRepo;

    private Map<String, Map<String, LanguageEntity>> localMap;
    private ArrayList<String> languages = new ArrayList<>(Arrays.asList("lv", "en"));

    @Override
    public void addNewLangKey(String key, ArrayList<String> languages) throws Exception {
        if (!languageRepo.findByKey(key).isEmpty())
            throw new Exception(messageLocale.getMessage("error.key.exists"));

        // Untranslated locales are set to ???
        for (String language : languages) {
            LanguageEntity languageEntity = new LanguageEntity(language, key, "???");
            languageRepo.save(languageEntity);
        }

        reloadFromDb();
    }

    @Override
    public LanguageEntity findByKeyAndLocale(String key, String locale) {
        if (localMap == null)
            reloadFromDb();

        // If doesn't exist, create with default values
        if (!localMap.containsKey(locale)) {
            try {
                addNewLangKey(key, languages);
            } catch (Exception ignored) {}
        }

        LanguageEntity content = localMap.get(locale).get(key);

        // If was recently generated, alert console
        if (content == null) {
            System.out.println("Couldn't get translate key: \n" + key + "\n(creating with default values...)");
            try {
                addNewLangKey(key, languages);
            } catch (Exception ignored) {}

            content = localMap.get(locale).get(key);
        }

        // Alert console for recurring missing translations
        if (content.getContent().equalsIgnoreCase("???"))
            System.out.println("Not translated: " + key);

        return content;
    }

    @Override
    public ArrayList<LanguageEntity> selectAllEntities() {
        ArrayList<LanguageEntity> toReturn = new ArrayList<>();

        for (Map<String, LanguageEntity> entry : localMap.values()) {
            toReturn.addAll(entry.values());
        }

        return toReturn;
    }

    @Override
    public void saveEntity(LanguageEntity languageEntity) throws Exception {
        if (languageEntity == null || languageEntity.getKey() == null)
            throw new Exception(messageLocale.getMessage("error.invalidLocaleId"));

        languageRepo.save(languageEntity);
    }

    @Override
    public void deleteByKey(String key) throws Exception {
        ArrayList<LanguageEntity> entity = languageRepo.findByKey(key);

        if (entity == null || entity.isEmpty())
            throw new Exception(messageLocale.getMessage("error.invalidLocaleId"));

        languageRepo.deleteAll(entity);
    }

    @Override
    public void reloadFromDb() {
        if (localMap != null)
            localMap.clear();
        localMap = new HashMap<>();

        ArrayList<LanguageEntity> allEntities = (ArrayList<LanguageEntity>) languageRepo.findAll();

        for (LanguageEntity entity : allEntities) {
            if (!localMap.containsKey(entity.getLocale()))
                localMap.put(entity.getLocale(), new HashMap<>());

            localMap.get(entity.getLocale()).put(entity.getKey(), entity);
        }
    }

    @Override
    public ArrayList<String> getLanguages() {
        return languages;
    }
}
