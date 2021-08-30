package lv.venta.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

@Component
public class MessageLocale {

    @Autowired
    private MessageSource messageSource;

    public String getMessage(String code) {
        code = code.replace("{", "").replace("}", "");
        return messageSource.getMessage(code, null, LocaleContextHolder.getLocale());
    }
}
