package lv.venta.controllers.admin;

import lv.venta.models.LanguageEntity;
import lv.venta.models.dto.LanguageDto;
import lv.venta.models.dto.SettingsDto;
import lv.venta.services.ILanguageService;
import lv.venta.services.ISettingsService;
import lv.venta.utils.MessageLocale;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.*;

@Controller
@RequestMapping("/admin")
public class AdminSettingsController {

    @Autowired
    private MessageLocale messageLocale;

    @Autowired
    ILanguageService languageService;

    @Autowired
    ISettingsService settingsService;

    @ModelAttribute("module")
    public String module() {
        return "admin/settings";
    }

    /**
     * Settings view contains language editing and
     * settings dto (terms, emails).
     */
    @GetMapping("/settings")
    public String viewSettings(Model model) {
        model.addAttribute("title", messageLocale.getMessage("{adminSettings.settings}"));

        // Prepares language map
        Map<String, LanguageDto> dtoMap = new HashMap<>();

        for (LanguageEntity languageEntity : languageService.selectAllEntities()) {
            if (!dtoMap.containsKey(languageEntity.getKey())) {
                LanguageDto dto = new LanguageDto();
                dto.setKey(languageEntity.getKey());
                dtoMap.put(languageEntity.getKey(), dto);
            }
        }

        for (Map.Entry<String, LanguageDto> entry : dtoMap.entrySet()) {
            for (String language : languageService.getLanguages()) {
                entry.getValue().getLangEntities().add(languageService.findByKeyAndLocale(entry.getKey(), language));
            }
        }

        model.addAttribute("languages", languageService.getLanguages());
        model.addAttribute("dtoList", new ArrayList<>(dtoMap.values()));

        // For settings dto
        model.addAttribute("settingsDto", settingsService.getSettings());

        return "admin/admin-settings";
    }

    /**
     * Handles terms and email template saving
     * with settings dto.
     */
    @PostMapping(value = "/edit-settings")
    public String editSettingsPost(Model model, SettingsDto settingsDto) {
        settingsService.setSettings(settingsDto);

        return "redirect:/admin/settings";
    }

    /**
     * Opens locale editing view by dto map index
     */
    @GetMapping("/edit-locale/{id}")
    public String editLocale(Model model, @PathVariable(name = "id") int id) {
        model.addAttribute("title", messageLocale.getMessage("{adminSettings.settings}"));

        Map<String, LanguageDto> dtoMap = new HashMap<>();

        for (LanguageEntity languageEntity : languageService.selectAllEntities()) {
            if (!dtoMap.containsKey(languageEntity.getKey())) {
                LanguageDto dto = new LanguageDto();
                dto.setKey(languageEntity.getKey());
                dtoMap.put(languageEntity.getKey(), dto);
            }
        }

        for (Map.Entry<String, LanguageDto> entry : dtoMap.entrySet()) {
            for (String language : languageService.getLanguages()) {
                entry.getValue().getLangEntities().add(languageService.findByKeyAndLocale(entry.getKey(), language));
            }
        }

        ArrayList<LanguageDto> dtoList = new ArrayList<>(dtoMap.values());

        if (id < 0 || id > dtoList.size() - 1) {
            model.addAttribute("errorMessage", messageLocale.getMessage("error.invalidLocaleId"));
            return "admin/error";
        }

        model.addAttribute("languageDto", dtoList.get(id));

        return "admin/admin-edit-locale";
    }

    /**
     * Edits locale with given language dto (contains key and new translations)
     */
    @PostMapping(value = "/edit-locale")
    public String editLocalePost(Model model, @Valid LanguageDto languageDto, BindingResult result) {
        for (LanguageEntity entity : languageDto.getLangEntities()) {
            try {
                languageService.saveEntity(entity);
            } catch (Exception e) {
                model.addAttribute("errorMessage", e.getMessage());
                return "admin/error";
            }
        }

        // Reloads cached translations from db
        languageService.reloadFromDb();

        return "redirect:/admin/settings";
    }

    /**
     * Deletes locale (in all languages) by given locale key
     */
    @GetMapping("/delete-locale/{key}")
    public String deleteLocale(Model model, @PathVariable(name = "key") String key) {
        model.addAttribute("title", messageLocale.getMessage("{adminSettings.settings}"));

        try {
            languageService.deleteByKey(key);
            languageService.reloadFromDb();
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "admin/error";
        }

        return "redirect:/admin/settings";
    }
}
