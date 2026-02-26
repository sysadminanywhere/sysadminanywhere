package com.sysadminanywhere.incident.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class SevenDigitOffsetDateTimeDeserializer extends JsonDeserializer<OffsetDateTime> {

    // Формат: до микросекунд (6 цифр), часовой пояс через +
    private static final DateTimeFormatter LOCAL_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS");

    @Override
    public OffsetDateTime deserialize(JsonParser p, DeserializationContext context) throws IOException {
        String text = p.getValueAsString().trim();
        if (text.isEmpty()) return null;

        try {
            // Пример: "2026-02-26T08:15:23.0000000+03:00"
            // Убираем 7-ю цифру из миллисекунд
            text = text.replaceAll("\\.(\\d{6})\\d", ".$1"); // → .000000

            // Разделяем на часть времени и смещение
            int tIndex = text.indexOf('T');
            int dotIndex = text.indexOf('.', tIndex);
            int plusIndex = text.lastIndexOf('+');
            int minusIndex = text.lastIndexOf('-');

            // Находим начало offset
            int offsetStart = Math.max(plusIndex, minusIndex);
            if (offsetStart <= dotIndex || offsetStart == -1) {
                throw new IllegalArgumentException("Invalid offset in datetime string: " + text);
            }

            String dateTimePart = text.substring(0, offsetStart); // "2026-02-26T08:15:23.000000"
            String offsetPart = text.substring(offsetStart);       // "+03:00"

            // Парсим LocalDateTime
            LocalDateTime localDateTime = LocalDateTime.parse(dateTimePart, LOCAL_FORMAT);

            // Парсим ZoneOffset
            ZoneOffset offset = ZoneOffset.of(offsetPart);

            // Собираем OffsetDateTime
            return OffsetDateTime.of(localDateTime, offset);

        } catch (Exception e) {
            throw new RuntimeException("Failed to parse OffsetDateTime from: " + text, e);
        }
    }

}