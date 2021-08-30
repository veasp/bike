package lv.venta.services.impl;

import lv.venta.enums.InquiryStatus;
import lv.venta.models.Inquiry;
import lv.venta.models.SettingEntity;
import lv.venta.services.IEmailService;
import lv.venta.services.ISettingsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.ArrayList;

@Service
public class EmailService implements IEmailService {

    @Autowired
    private JavaMailSender emailSender;

    @Autowired
    private Environment env;

    @Autowired
    ISettingsService settingsService;

    @Async
    public void sendInquiryStatusChangeEmail(Inquiry inquiry) {
        MimeMessage mimeMessage = emailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");
            SettingEntity entity = null;

            if (inquiry.getInquiryStatus() == InquiryStatus.ACCEPTED)
                entity = settingsService.getByKey("emailAccepted");
            else
                entity = settingsService.getByKey("emailRejected");

            helper.setSubject("VentRent - Inquiry status changed");
            helper.setText(entity.getContent(), true);
            if (env.getProperty("spring.mail.from") != null)
                helper.setFrom(env.getProperty("spring.mail.from"));
            helper.setTo(inquiry.getInquiryUser().getEmail());

            emailSender.send(mimeMessage);
        } catch (MessagingException ignored) {}
    }

    @Async
    public void sendNewInquiryEmail(ArrayList<String> emails) {
        MimeMessage mimeMessage = emailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");
            SettingEntity entity = null;

            entity = settingsService.getByKey("emailNewInquiry");

            helper.setSubject("VentRent - New Inquiry");
            helper.setText(entity.getContent(), true);

            for (int i = 0; i < emails.size(); i++) {
                helper.addTo(emails.get(i));
            }

            if (env.getProperty("spring.mail.from") != null)
                helper.setFrom(env.getProperty("spring.mail.from"));

            emailSender.send(mimeMessage);
        } catch (MessagingException ignored) {}
    }
}
