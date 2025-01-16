package org.example.fileprocessingservice.controller;

import org.example.fileprocessingservice.dto.ParsedResult;
import org.example.fileprocessingservice.service.FileProcessingService;
import org.example.fileprocessingservice.service.NStrParserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/v1/parser")
public class NStrParserController {
    private final NStrParserService nStrParserService;
    private final FileProcessingService fileProcessingService;
    private static final Logger logger = LoggerFactory.getLogger(NStrParserController.class);

    public NStrParserController(NStrParserService nStrParserService, FileProcessingService fileProcessingService) {
        this.nStrParserService = nStrParserService;
        this.fileProcessingService = fileProcessingService;
    }

    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    public ResponseEntity<List<ParsedResult>> uploadFile(@RequestParam("file") MultipartFile file) throws IOException {
        try {
            // Логируем базовую информацию о файле
            logger.info("Загружен файл: {}", file.getOriginalFilename());
            logger.info("Размер файла: {} байт", file.getSize());

            if (file.isEmpty()) {
                throw new RuntimeException("Файл пуст.");
            }

            List<String> rawStrings = fileProcessingService.processFileFromStream(file.getInputStream());
            List<ParsedResult> parsedResults = nStrParserService.parseNStrStrings(rawStrings);
            return ResponseEntity.ok(parsedResults);
        } catch (IOException e) {
            logger.error("Ошибка при получении входного потока файла: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(List.of(new ParsedResult() {{
                        setOriginalString("Error");
                        setTranslations(Map.of("error", "Ошибка при обработке файла: " + e.getMessage()));
                    }}));
        } catch (RuntimeException e) {
            logger.error("Ошибка обработки файла: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(List.of(new ParsedResult() {{
                        setOriginalString("Error");
                        setTranslations(Map.of("error", "Ошибка при обработке файла: " + e.getMessage()));
                    }}));
        }
    }




    @PostMapping("/save")
    public ResponseEntity<String> saveParsedResultsToFile(
            @RequestBody List<ParsedResult> parsedResults,
            @RequestParam("directory") String directory,
            @RequestParam("fileName") String fileName) {
        if (parsedResults.isEmpty()) {
            return ResponseEntity.badRequest().body("No parsed results available to save.");
        }

        try {
            String filePath = nStrParserService.saveParsedResults(parsedResults, directory, fileName);
            return ResponseEntity.ok("Parsed results saved successfully to: " + filePath);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to save parsed results: " + e.getMessage());
        }
    }@PostMapping("/clear")
    public ResponseEntity<String> clearTemporaryFile() {
        try {
            nStrParserService.clearTemporaryResults("temporary_results.json");
            return ResponseEntity.ok("Temporary file cleared successfully.");
        } catch (Exception e) {
            logger.error("Failed to clear temporary file: {}", e.getMessage());
            return ResponseEntity.status(500).body("Failed to clear temporary file: " + e.getMessage());
        }
    }

}
