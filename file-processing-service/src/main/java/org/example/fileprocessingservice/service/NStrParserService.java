package org.example.fileprocessingservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.fileprocessingservice.dto.ParsedResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class NStrParserService {

    private static final Logger logger = LoggerFactory.getLogger(NStrParserService.class);
    private final FileStorageService fileStorageService;
    private final List<ParsedResult> temporaryResults;
    @Value("${temp.file.clear:false}")
    private boolean clearTemporaryFile;

    public NStrParserService(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;

          // очистка временного файла, если включено в настройках
        if (clearTemporaryFile) {
            clearTemporaryResults("temporary_results.json");
        }
        //инициализ временных данных
        this.temporaryResults = loadTemporaryResults("temporary_results.json");
    }


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
        updateTemporaryResults(result); // обновляем временные результаты
        saveTemporaryResults("temporary_results.json"); // сохраняем временные результаты
        return result;
    }


    public List<ParsedResult> loadTemporaryResults(String filePath) {
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                return new ArrayList<>(); // возвращаем пустой изменяем списк, если файл не найден
            }
            return new ArrayList<>(Arrays.asList(new ObjectMapper().readValue(file, ParsedResult[].class)));
        } catch (IOException e) {
            throw new RuntimeException("Ошибка загрузки временных результатов: " + e.getMessage());
        }
    }

    public void saveTemporaryResults(String filePath) {
        try {
            fileStorageService.saveTemporaryResults(temporaryResults, filePath);
            logger.info("Temporary results saved successfully to {}", filePath);
        } catch (RuntimeException e) {
            logger.error("Failed to save temporary results: {}", e.getMessage());
        }
    }

    public void updateTemporaryResults(List<ParsedResult> newResults) {
        Set<String> existingStrings = new HashSet<>();
        for (ParsedResult result : temporaryResults) {
            existingStrings.add(result.getOriginalString());
        }

        for (ParsedResult newResult : newResults) {
            if (!existingStrings.contains(newResult.getOriginalString())) {
                temporaryResults.add(newResult);
            }
        }
    }


    public String saveParsedResults(List<ParsedResult> parsedResults, String directory, String fileName) {
        // загружаем временые данные из JSON
        List<ParsedResult> existingResults = loadTemporaryResults("temporary_results.json");

        // создаю множество для проверки уникальности
        Set<String> existingOriginalStrings = new HashSet<>();
        for (ParsedResult result : existingResults) {
            existingOriginalStrings.add(result.getOriginalString());
        }

        // добавляю только уникальные результаты
        for (ParsedResult newResult : parsedResults) {
            if (!existingOriginalStrings.contains(newResult.getOriginalString())) {
                existingResults.add(newResult);
            }
        }

        //определяем путь к файлу для сохранения
        String filePath = Paths.get(directory, fileName).toString();
        try (FileWriter writer = new FileWriter(filePath)) {
            // записываем данные в файл
            int lineNumber = 1; // Счётчик строки для отображения
            for (ParsedResult result : existingResults) {
                for (Map.Entry<String, String> entry : result.getTranslations().entrySet()) {
                    String languageCode = entry.getKey();
                    String text = entry.getValue();

                    // Записываем строку в нужном нам формате «Номер строки : язык : строка на языке»
                    writer.write(String.format("%d: %s : %s", lineNumber, languageCode, text));
                    writer.write(System.lineSeparator()); // Добавляем новую строку
                }
                lineNumber++;
            }
            // сохраняю временные данные в JSON
            saveTemporaryResults("temporary_results.json");

            // логирую успешное сохранение
            logger.info("Parsed results saved successfully to {}", filePath);
        } catch (IOException e) {
            // логируем ошибку и выбрасываем исключение
            logger.error("Error while saving parsed results: {}", e.getMessage());
            throw new RuntimeException("Ошибка при сохранении файла: " + e.getMessage());
        }
        return filePath;
    }





    private ParsedResult parseNStr(String str) {
        try {
            Pattern pattern = Pattern.compile("NStr\\(\"([^\"]+)\"\\)");
            Matcher matcher = pattern.matcher(str);

            if (matcher.find()) {
                String content = matcher.group(1);
                Map<String, String> translations = extractTranslations(content);

                ParsedResult result = new ParsedResult();
                result.setOriginalString(str);
                result.setTranslations(translations);
                return result;
            } else {
                logger.warn("Не удалось найти NStr в строке: {}", str);
            }
        } catch (Exception e) {
            logger.error("Ошибка парсинга строки: {}", str, e);
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

    public void clearTemporaryResults(String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            boolean deleted = file.delete();
            if (deleted) {
                logger.info("Temporary file cleared: {}", filePath);
            } else {
                logger.warn("Failed to clear temporary file: {}", filePath);
            }
        }
    }




}
