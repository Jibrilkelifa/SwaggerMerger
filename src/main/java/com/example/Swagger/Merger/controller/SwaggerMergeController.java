package com.example.Swagger.Merger.controller;

import com.example.Swagger.Merger.service.SwaggerMergeService;
import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class SwaggerMergeController {

    @Autowired
    private SwaggerMergeService swaggerMergeService;


    @PostMapping("/upload")
    public void uploadSwaggerFile(@RequestParam("file") MultipartFile file) throws IOException {
        String fileName = file.getOriginalFilename();
        String fileContent = new String(file.getBytes());

        swaggerMergeService.saveSwaggerFile(fileName, fileContent);
    }

    @GetMapping("/merge")
    public String mergeSwaggerJson() {
        return swaggerMergeService.mergeSwaggerJson();
    }
}
