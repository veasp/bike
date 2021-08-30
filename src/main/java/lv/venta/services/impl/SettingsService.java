package lv.venta.services.impl;

import lv.venta.models.SettingEntity;
import lv.venta.models.dto.SettingsDto;
import lv.venta.repositories.SettingEntityRepo;
import lv.venta.services.ISettingsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SettingsService implements ISettingsService {

    @Autowired
    SettingEntityRepo settingRepo;

    @Override
    public SettingsDto getSettings() {
        SettingsDto settingsDto = new SettingsDto();

        SettingEntity rules = settingRepo.findByKey("rules");
        if (rules == null) {
            rules = new SettingEntity("rules", "Default setting value");
            rules = settingRepo.save(rules);
        }
        settingsDto.setRules(rules.getContent());

        SettingEntity aboutUs = settingRepo.findByKey("aboutUs");
        if (aboutUs == null) {
            aboutUs = new SettingEntity("aboutUs", "Default setting value");
            aboutUs = settingRepo.save(aboutUs);
        }
        settingsDto.setAboutUs(aboutUs.getContent());

        SettingEntity emailAccepted = settingRepo.findByKey("emailAccepted");
        if (emailAccepted == null) {
            emailAccepted = new SettingEntity("emailAccepted", "Default setting value");
            emailAccepted = settingRepo.save(emailAccepted);
        }
        settingsDto.setEmailAccepted(emailAccepted.getContent());

        SettingEntity emailRejected = settingRepo.findByKey("emailRejected");
        if (emailRejected == null) {
            emailRejected = new SettingEntity("emailRejected", "Default setting value");
            emailRejected = settingRepo.save(emailRejected);
        }
        settingsDto.setEmailRejected(emailRejected.getContent());

        SettingEntity emailNewInquiry = settingRepo.findByKey("emailNewInquiry");
        if (emailNewInquiry == null) {
            emailNewInquiry = new SettingEntity("emailNewInquiry", "Default setting value");
            emailNewInquiry = settingRepo.save(emailNewInquiry);
        }
        settingsDto.setEmailNewInquiry(emailNewInquiry.getContent());

        return settingsDto;
    }

    @Override
    public void setSettings(SettingsDto settingsDto) {
        if (settingsDto.getRules() != null) {
            SettingEntity rules = settingRepo.findByKey("rules");
            if (rules != null) {
                rules.setContent(settingsDto.getRules());
                settingRepo.save(rules);
            }
        }

        if (settingsDto.getAboutUs() != null) {
            SettingEntity aboutUs = settingRepo.findByKey("aboutUs");
            if (aboutUs != null) {
                aboutUs.setContent(settingsDto.getAboutUs());
                settingRepo.save(aboutUs);
            }
        }

        if (settingsDto.getEmailAccepted() != null) {
            SettingEntity emailAccepted = settingRepo.findByKey("emailAccepted");
            if (emailAccepted != null) {
                emailAccepted.setContent(settingsDto.getEmailAccepted());
                settingRepo.save(emailAccepted);
            }
        }

        if (settingsDto.getEmailRejected() != null) {
            SettingEntity emailRejected = settingRepo.findByKey("emailRejected");
            if (emailRejected != null) {
                emailRejected.setContent(settingsDto.getEmailRejected());
                settingRepo.save(emailRejected);
            }
        }

        if (settingsDto.getEmailNewInquiry() != null) {
            SettingEntity emailNewInquiry = settingRepo.findByKey("emailNewInquiry");
            if (emailNewInquiry != null) {
                emailNewInquiry.setContent(settingsDto.getEmailNewInquiry());
                settingRepo.save(emailNewInquiry);
            }
        }
    }

    @Override
    public SettingEntity getByKey(String key) {
        SettingEntity entity = settingRepo.findByKey(key);

        if (entity == null) {
            entity = new SettingEntity(key, "Default setting value");
            entity = settingRepo.save(entity);
        }

        return entity;
    }
}
