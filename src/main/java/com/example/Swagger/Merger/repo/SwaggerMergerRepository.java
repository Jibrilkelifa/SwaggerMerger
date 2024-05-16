package com.example.Swagger.Merger.repo;

import com.example.Swagger.Merger.model.SwaggerFile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SwaggerMergerRepository extends JpaRepository<SwaggerFile, Long> {
    // Add any custom query methods if needed
}
