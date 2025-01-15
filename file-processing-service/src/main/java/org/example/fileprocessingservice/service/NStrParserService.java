package org.example.fileprocessingservice.service;

import org.example.fileprocessingservice.dto.ParsedResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class NStrParserService {

    private static final Logger logger = LoggerFactory.getLogger(NStrParserService.class);

    public List<ParsedResult> parseNStrStrings(List<String> strings) {
        List<ParsedResult> result = new ArrayList<>();
        for (String str : strings) {
            if (str.contains("NStr")) {
                ParsedResult parsedData = parseNStr(str);
                if (parsedData != null) {
                    result.add(parsedData);
                }
            }
        }
        return result;
    }

    public String saveParsedResults(List<ParsedResult> parsedResults, String directory, String fileName) {
        String filePath = Paths.get(directory, fileName).toString();
        try (FileWriter writer = new FileWriter(filePath)) {
            for (ParsedResult result : parsedResults) {
                writer.write("Original String: " + result.getOriginalString() + System.lineSeparator());
                writer.write("Translations: " + result.getTranslations() + System.lineSeparator());
                writer.write(System.lineSeparator());
            }
            logger.info("Parsed results saved successfully to {}", filePath);
        } catch (IOException e) {
            logger.error("Error while saving parsed results: {}", e.getMessage());
            throw new RuntimeException("Ошибка при сохранении файла: " + e.getMessage());
        }
        return filePath;
    }

    private ParsedResult parseNStr(String str) {
        Pattern pattern = Pattern.compile("NStr\\(\"([^\"]+)\"\\)");
        Matcher matcher = pattern.matcher(str);

        if (matcher.find()) {
            String content = matcher.group(1);
            Map<String, String> translations = extractTranslations(content);

            ParsedResult result = new ParsedResult();
            result.setOriginalString(str);
            result.setTranslations(translations);
            return result;
        }

        return null;
    }

    private Map<String, String> extractTranslations(String content) {
        Map<String, String> translations = new HashMap<>();
        String[] parts = content.split(";");
        for (String part : parts) {
            String[] keyValue = part.split("=");
            if (keyValue.length == 2) {
                String language = keyValue[0].trim();
                String value = keyValue[1].trim().replace("'", "").replace("\"", "");
                translations.put(language, value);
            }
        }
        return translations;
    }
}
