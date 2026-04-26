package com.getfriedpig.golem.fileio;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;
public class FileManager {
    public static final String MasterFolder = "GolemScripts";

    public static String FileReadRequest(UUID playerId, String fileName) {
        File file = new File(MasterFolder, playerId + "/" + fileName + ".txt");
        if (file.exists()) {
            return readFileAsString(file.getPath());
        }
        return null;
    }
    private static String readFileAsString(String filename) {
        try {
            return new String(Files.readAllBytes(Paths.get(filename)));
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
            return null;
        }
    }

    public static void FileWriteRequest(UUID playerId, String fileName, String content) {
        File file = new File(MasterFolder, playerId + "/" + fileName + ".txt");
        saveStringToFile(file.getPath(), content);
    }

    public static void saveStringToFile(String filename, String content) {
        try {
            File file = new File(filename);

            // Ensure parent directories exist
            File parent = file.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs(); // creates all missing folders
            }

            try (FileWriter writer = new FileWriter(file)) {
                writer.write(content);
            }

            System.out.println("File \"" + filename + "\" created successfully.");
        } catch (IOException e) {
            System.err.println("Error writing file: " + e.getMessage());
        }
    }

}
