package com.naprock.hexudon.util;

import com.naprock.hexudon.domain.exception.system.ConfigLoadException;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class FileUtils {

    public static List<String> readLinesFromResource(String fileName) {
        ClassLoader classLoader = FileUtils.class.getClassLoader();

        InputStream inputStream = classLoader.getResourceAsStream(fileName);

        if (inputStream == null) {
            throw new ConfigLoadException("File not found in resources: " + fileName);
        }

        List<String> lines = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {

            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }

        } catch (Exception e) {
            throw new ConfigLoadException("Failed to read file: " + fileName, e);
        }

        return lines;
    }
}
