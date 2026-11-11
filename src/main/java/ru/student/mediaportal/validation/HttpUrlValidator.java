package ru.student.mediaportal.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.net.URI;

public class HttpUrlValidator implements ConstraintValidator<HttpUrl, String> {
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // Отсутствующее значение отдельно проверяется через @NotBlank.
        if (value == null || value.isBlank()) return true;
        try {
            URI uri = URI.create(value);
            boolean http = "http".equalsIgnoreCase(uri.getScheme())
                    || "https".equalsIgnoreCase(uri.getScheme());
            return http && uri.getHost() != null && !uri.getHost().isBlank()
                    && (uri.getPort() == -1 || uri.getPort() >= 1 && uri.getPort() <= 65535);
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }
}
