package com.getfriedpig.golem.fileio;

import java.io.FileWriter;
import java.io.IOException;

public class WriteToFile {

    public static void saveStringToFile(String filename, String content) {
        try (FileWriter writer = new FileWriter(filename)) {
            writer.write(content);
            System.out.println("File \"" + filename + "\" created successfully.");
        } catch (IOException e) {
            System.err.println("Error writing file: " + e.getMessage());
        }
    }
}