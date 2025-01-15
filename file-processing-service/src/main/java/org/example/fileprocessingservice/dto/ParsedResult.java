package org.example.fileprocessingservice.dto;

import java.util.Map;

public class ParsedResult {
    private String originalString;
    private Map<String, String> translations;

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
