package lat.luisdias.stock_app_main_service.stock.infra.util;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

@Component
public class I18n {
    private static MessageSource messageSource;

    public I18n(MessageSource messageSrc) {
        messageSource = messageSrc;
    }

    public static String get(String code, Object... args) {
        return messageSource.getMessage(code, args, LocaleContextHolder.getLocale());
    }

    public static String get(String code) {
        return messageSource.getMessage(code, null, LocaleContextHolder.getLocale());
    }
}
