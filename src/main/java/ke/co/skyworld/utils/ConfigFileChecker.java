package ke.co.skyworld.utils;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ConfigFileChecker {
    public static File getConfigFile() {
        Path folderPath = Paths.get("config", "config.xml");
        if (!Files.exists(folderPath)) {
            throw new IllegalArgumentException("Configuration file not found at " + folderPath);
        }
        return folderPath.toFile();
    }
}
