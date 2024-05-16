package com.example.Swagger.Merger.service;

import com.example.Swagger.Merger.model.SwaggerFile;
import com.example.Swagger.Merger.repo.SwaggerMergerRepository;
import com.fasterxml.jackson.core.JsonParser;
import com.google.gson.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
public class SwaggerMergeService {
    private final SwaggerMergerRepository swaggerMergerRepository;

    public SwaggerMergeService(SwaggerMergerRepository swaggerMergerRepository) {
        this.swaggerMergerRepository = swaggerMergerRepository;
    }

    public void saveSwaggerFile(String fileName, String fileContent) {
        SwaggerFile swaggerFile = new SwaggerFile();
        swaggerFile.setFileName(fileName);
        swaggerFile.setFileContent(fileContent);

        swaggerMergerRepository.save(swaggerFile);
    }

    public String mergeSwaggerJson() {
        // Retrieve the list of Swagger files from the repository
        List<SwaggerFile> swaggerFiles = swaggerMergerRepository.findAll();

        // Create an empty JsonObject to hold the merged Swagger data
        JsonObject mergedSwaggerJson = new JsonObject();

        // Extract the OpenAPI version from the first Swagger file
        String openapiVersion = null;
        if (!swaggerFiles.isEmpty()) {
            String firstSwaggerContent = swaggerFiles.get(0).getFileContent();
            Gson gson = new GsonBuilder().setPrettyPrinting().create(); // Instantiate Gson with pretty printing
            JsonObject firstSwaggerJson = gson.fromJson(firstSwaggerContent, JsonObject.class);
            if (firstSwaggerJson.has("openapi")) {
                openapiVersion = firstSwaggerJson.get("openapi").getAsString();
            }
        }

        // Add the "openapi" field if the version is available
        if (openapiVersion != null) {
            mergedSwaggerJson.addProperty("openapi", openapiVersion);
        }

        // Create a Gson instance with pretty printing
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        // Set the static "info" section
        JsonObject staticInfoObject = new JsonObject();
        staticInfoObject.addProperty("title", "Merged Swagger API");
        staticInfoObject.addProperty("description", "API Definition of Merged Swagger");
        staticInfoObject.addProperty("version", "1.0.0");
        mergedSwaggerJson.add("info", staticInfoObject);

        // Merge the sections of each Swagger file
        for (SwaggerFile swaggerFile : swaggerFiles) {
            String swaggerContent = swaggerFile.getFileContent();
            JsonObject swaggerJson = gson.fromJson(swaggerContent, JsonObject.class);

            // Merge the 'servers' section
            if (swaggerJson.has("servers")) {
                JsonArray serversArray = mergedSwaggerJson.getAsJsonArray("servers");
                if (serversArray == null) {
                    serversArray = new JsonArray();
                    mergedSwaggerJson.add("servers", serversArray);
                }
                JsonArray swaggerServersArray = swaggerJson.getAsJsonArray("servers");
                if (swaggerServersArray != null) {
                    for (JsonElement server : swaggerServersArray) {
                        serversArray.add(server);
                    }
                }
            }

            // Merge the 'security' section
            if (swaggerJson.has("security")) {
                mergedSwaggerJson.add("security", swaggerJson.get("security"));
            }

            // Merge the 'paths' section
            if (swaggerJson.has("paths")) {
                JsonObject pathsObject = mergedSwaggerJson.getAsJsonObject("paths");
                if (pathsObject == null) {
                    pathsObject = new JsonObject();
                    mergedSwaggerJson.add("paths", pathsObject);
                }
                JsonObject swaggerPathsObject = swaggerJson.getAsJsonObject("paths");
                if (swaggerPathsObject != null) {
                    for (Map.Entry<String, JsonElement> entry : swaggerPathsObject.entrySet()) {
                        String path = entry.getKey();
                        JsonElement pathElement = entry.getValue();
                        pathsObject.add(path, pathElement);
                    }
                }
            }

            // Merge the 'components' section
            if (swaggerJson.has("components")) {
                JsonObject componentsObject = mergedSwaggerJson.getAsJsonObject("components");
                if (componentsObject == null) {
                    componentsObject = new JsonObject();
                    mergedSwaggerJson.add("components", componentsObject);
                }
                JsonObject swaggerComponentsObject = swaggerJson.getAsJsonObject("components");
                if (swaggerComponentsObject != null) {
                    for (Map.Entry<String, JsonElement> entry : swaggerComponentsObject.entrySet()) {
                        String componentName = entry.getKey();
                        JsonElement componentElement = entry.getValue();
                        componentsObject.add(componentName, componentElement);
                    }
                }
            }
        }

        // Convert the merged JsonObject to a string with pretty printing
        String mergedSwaggerJsonString = gson.toJson(mergedSwaggerJson);

        return mergedSwaggerJsonString;
    }
}

