package com.naprock.hexudon.util;

import com.naprock.hexudon.domain.exception.system.ConfigLoadException;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class FileUtilsTest {

    @Test
    void testReadLinesFromResource() {
        List<String> lines = FileUtils.readLinesFromResource("sample.txt");

        assertAll(
            () -> assertNotNull(lines),
            () -> assertEquals(3, lines.size()),
            () -> assertEquals("line 1", lines.get(0)),
            () -> assertEquals("line 2", lines.get(1)),
            () -> assertEquals("line 3", lines.get(2))
        );
    }

    @Test
    void testReadLinesFromResourceFileNotFound() {
        ConfigLoadException ex = assertThrows(ConfigLoadException.class, 
            () -> FileUtils.readLinesFromResource("non_existent_file_xyz.txt"),
            "Reading non-existent resource should throw ConfigLoadException"
        );
        assertTrue(ex.getMessage().contains("File not found in resources"));
    }
}