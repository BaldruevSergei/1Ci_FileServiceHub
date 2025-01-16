package service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.fileprocessingservice.dto.ParsedResult;
import org.example.fileprocessingservice.service.FileStorageService;
import org.example.fileprocessingservice.service.NStrParserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NStrParserServiceTest {

    private FileStorageService fileStorageService; // Мок сервиса хранения файлов
    private NStrParserService nStrParserService;   // Сервис, который тестируем

    @BeforeEach
    void setUp() {
        fileStorageService = mock(FileStorageService.class);
        nStrParserService = new NStrParserService(fileStorageService);

        // Создаем тестовую директорию, если она отсутствует
        File testDir = new File("test-dir");
        if (!testDir.exists()) {
            testDir.mkdirs();
        }
    }


    @Test
    void testClearTemporaryResults() {
        // Проверка удаления временных результатов
        String filePath = "temporary_results.json";
        File fileMock = mock(File.class);
        when(fileMock.exists()).thenReturn(true);
        when(fileMock.delete()).thenReturn(true);

        // Вызов метода
        nStrParserService.clearTemporaryResults(filePath);

        // Убеждаемся, что метод saveTemporaryResults не был вызван
        verify(fileStorageService, never()).saveTemporaryResults(anyList(), anyString());
    }

    @Test
    void testLoadTemporaryResults() throws IOException {
        // Тест загрузки временных результатов
        String filePath = "temporary_results.json";
        List<ParsedResult> mockResults = List.of(
                new ParsedResult("Test", Map.of("en", "Test"))
        );

        when(fileStorageService.loadTemporaryResults(filePath)).thenReturn(mockResults);

        // Вызов метода
        List<ParsedResult> results = nStrParserService.loadTemporaryResults(filePath);

        // Проверки
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("Test", results.get(0).getOriginalString());
        verify(fileStorageService, times(1)).loadTemporaryResults(filePath);
    }

    @Test
    void testSaveTemporaryResults() {
        // Тест сохранения временных результатов
        String filePath = "temporary_results.json";
        List<ParsedResult> results = List.of(new ParsedResult("Test", Map.of("en", "Test")));

        // Вызов метода
        nStrParserService.saveTemporaryResults(filePath);

        // Убеждаемся, что метод сохранения был вызван
        verify(fileStorageService, times(1)).saveTemporaryResults(anyList(), eq(filePath));
    }

    @Test
    void testParseNStrStrings() {
        // Тест парсинга строк NStr
        List<String> rawStrings = List.of(
                "NStr(\"en = 'Hello'; ru = 'Привет'\")",
                "NStr(\"en = 'Goodbye'; ru = 'До свидания'\")"
        );

        // Вызов метода
        List<ParsedResult> results = nStrParserService.parseNStrStrings(rawStrings);

        // Проверки
        assertNotNull(results);
        assertEquals(2, results.size());
        assertEquals("Hello", results.get(0).getTranslations().get("en"));
        assertEquals("Привет", results.get(0).getTranslations().get("ru"));
    }


    @Test
    void testSaveParsedResults() {
        // Тест сохранения распарсенных результатов
        String directory = "test-dir";
        String fileName = "output.txt";
        String expectedFilePath = Paths.get(directory, fileName).toString();

        List<ParsedResult> parsedResults = List.of(
                new ParsedResult("NStr(\"en = 'Hello'; ru = 'Привет'\")", Map.of("en", "Hello", "ru", "Привет"))
        );

        doNothing().when(fileStorageService).saveTemporaryResults(anyList(), anyString());

        // Вызов метода
        String resultFilePath = nStrParserService.saveParsedResults(parsedResults, directory, fileName);

        // Проверки
        assertEquals(expectedFilePath, resultFilePath);
        verify(fileStorageService, times(1)).saveTemporaryResults(parsedResults, "temporary_results.json");
    }

    @Test
    void testSaveParsedResultsWithDuplicateEntries() {
        // Тест обработки дубликатов при сохранении результатов
        String directory = "test-dir";
        String fileName = "output.txt";
        String expectedFilePath = Paths.get(directory, fileName).toString();

        List<ParsedResult> parsedResults = List.of(
                new ParsedResult("NStr(\"en = 'Hello'; ru = 'Привет'\")", Map.of("en", "Hello", "ru", "Привет")),
                new ParsedResult("NStr(\"en = 'Hello'; ru = 'Привет'\")", Map.of("en", "Hello", "ru", "Привет"))
        );

        doNothing().when(fileStorageService).saveTemporaryResults(anyList(), anyString());

        // Вызов метода
        String resultFilePath = nStrParserService.saveParsedResults(parsedResults, directory, fileName);

        // Проверки
        assertEquals(expectedFilePath, resultFilePath);
        verify(fileStorageService, times(1)).saveTemporaryResults(anyList(), eq("temporary_results.json"));
    }
}
