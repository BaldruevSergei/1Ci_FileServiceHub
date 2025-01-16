package org.example.fileprocessingservice;
import jakarta.annotation.PostConstruct;
import org.example.fileprocessingservice.service.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class FileProcessingServiceApplication {

    private final FileStorageService fileStorageService;

    public FileProcessingServiceApplication(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    @PostConstruct
    public void setup() {
        fileStorageService.initializeTempDirectory();
    }

    public static void main(String[] args) {
        SpringApplication.run(FileProcessingServiceApplication.class, args);
    }
}
