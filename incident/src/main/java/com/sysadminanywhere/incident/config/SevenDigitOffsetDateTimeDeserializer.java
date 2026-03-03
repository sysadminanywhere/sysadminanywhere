package com.sysadminanywhere.incident.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class SevenDigitOffsetDateTimeDeserializer extends JsonDeserializer<OffsetDateTime> {

    // Шаблон для формата с 7 знаками после запятой (Java Time сам округлит до наносекунд)
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    @Override
    public OffsetDateTime deserialize(JsonParser p, DeserializationContext context) throws IOException {
        String text = p.getValueAsString();
        if (text == null || text.trim().isEmpty()) {
            return null;
        }

        text = text.trim();

        try {
            // Пробуем распарсить строку напрямую
            return OffsetDateTime.parse(text, FORMATTER);
        } catch (DateTimeParseException e) {
            // Если не удалось, возможно, проблема с 7-ю цифрами после точки
            // Убираем последнюю цифру из секунд (микросекунды до 6 знаков)
            text = truncateMicroseconds(text);

            try {
                return OffsetDateTime.parse(text, FORMATTER);
            } catch (DateTimeParseException ex) {
                throw new RuntimeException("Failed to parse OffsetDateTime from: " + text, ex);
            }
        }
    }

    // Удаляет последнюю цифру после точки, если их больше 6
    private String truncateMicroseconds(String input) {
        int dotIndex = input.lastIndexOf('.');
        if (dotIndex == -1) {
            return input; // нет точки — ничего не меняем
        }

        int offsetIndex = Math.max(input.lastIndexOf('+'), input.lastIndexOf('-'));
        if (offsetIndex <= dotIndex) {
            offsetIndex = input.length(); // если нет смещения, до конца
        }

        String fraction = input.substring(dotIndex + 1, offsetIndex);

        if (fraction.length() > 6) {
            String truncatedFraction = fraction.substring(0, 6);
            return input.substring(0, dotIndex + 1) + truncatedFraction + input.substring(offsetIndex);
        }

        return input;
    }

}