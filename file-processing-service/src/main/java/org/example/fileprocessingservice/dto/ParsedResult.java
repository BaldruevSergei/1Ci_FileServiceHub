package org.example.fileprocessingservice.dto;

import java.util.Map;

public class ParsedResult {
    private String originalString;
    private Map<String, String> translations;

    // Конструктор без аргументов (если нужен для сериализации/десериализации)
    public ParsedResult() {}

    // Новый конструктор с аргументами
    public ParsedResult(String originalString, Map<String, String> translations) {
        this.originalString = originalString;
        this.translations = translations;
    }

    // Геттеры и сеттеры
    public String getOriginalString() {
        return originalString;
    }

    public void setOriginalString(String originalString) {
        this.originalString = originalString;
    }

    public Map<String, String> getTranslations() {
        return translations;
    }

    public void setTranslations(Map<String, String> translations) {
        this.translations = translations;
    }
}
