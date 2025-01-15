package org.example.fileprocessingservice.controller;

import org.example.fileprocessingservice.dto.ParsedResult;
import org.example.fileprocessingservice.service.FileProcessingService;
import org.example.fileprocessingservice.service.NStrParserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/parser")
public class NStrParserController {

    private final NStrParserService nStrParserService;
    private final FileProcessingService fileProcessingService;

    public NStrParserController(NStrParserService nStrParserService, FileProcessingService fileProcessingService) {
        this.nStrParserService = nStrParserService;
        this.fileProcessingService = fileProcessingService;
    }

    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    public ResponseEntity<List<ParsedResult>> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            List<String> rawStrings = fileProcessingService.processFileFromStream(file.getInputStream());
            List<ParsedResult> parsedResults = nStrParserService.parseNStrStrings(rawStrings);
            return ResponseEntity.ok(parsedResults);
        } catch (Exception e) {
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
    }
}
