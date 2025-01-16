package org.example.fileprocessingservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.fileprocessingservice.dto.ParsedResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Service
public class FileStorageService {

    @Value("${temp.file.path}")
    private String tempFilePath;


    /**
     * Инициализирует временную директорию для хранения файлов.
     * Если директория не существует, она создаётся.
     */
    public void initializeTempDirectory() {
        File tempDir = new File(tempFilePath);
        if (!tempDir.exists()) {
            boolean created = tempDir.mkdirs();
            if (!created) {
                throw new RuntimeException("Не удалось создать временную директорию: " + tempFilePath);
            }
        }
    }

    /*
     * Загружает временные результаты из JSON-файла.
     *
     * @param filePath путь к JSON-файлу
     * @return список объектов ParsedResult */

    public List<ParsedResult> loadTemporaryResults(String filePath) {
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                throw new RuntimeException("Файл с временными результатами не найден: " + filePath);
            }

            // Чтение и десериализация данных
            return Arrays.asList(new ObjectMapper().readValue(file, ParsedResult[].class));
        } catch (IOException e) {
            throw new RuntimeException("Ошибка загрузки временных результатов: " + e.getMessage());
        }
    }

    /**
     * Сохраняет временные результаты в JSON-файл.
     *
     * @param results список объектов ParsedResult
     * @param filePath путь для сохранения JSON-файла
     */
    public void saveTemporaryResults(List<ParsedResult> results, String filePath) {
        try {
            File file = new File(filePath);
            new ObjectMapper().writeValue(file, results);
        } catch (IOException e) {
            throw new RuntimeException("Ошибка сохранения временных результатов: " + e.getMessage());
        }
    }

    /**
     * Возвращает путь к временной директории, заданной в application.properties.
     *
     * @return путь к временной директории
     */
    public String getTemporaryDirectory() {
        return tempFilePath;
    }
}
