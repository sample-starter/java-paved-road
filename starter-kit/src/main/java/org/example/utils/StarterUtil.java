package org.example.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.stream.Stream;

public class StarterUtil {

    public static void copySourceModuleToLocalPath(String source, Path destinationPath) throws IOException {
        Path sourcePath = Paths.get(source);
        try (Stream<Path> paths = Files.walk(sourcePath)) {
            for (Path path : paths.toList()) {
                Path destination = destinationPath.resolve(sourcePath.relativize(path));
                if (Files.isDirectory(path)) {
                    if (!Files.exists(destination)) {
                        Files.createDirectories(destination);
                    }
                } else {
                    if(destination.toString().contains("src/main/java/")) {
                        String content = new String(Files.readAllBytes(path));
//                        String newPackagePath = destination.getParent().toString().split("src/main/java/")[1].replace('/','.');
//                        String updatedContent = content.replaceFirst("package\\s+[^;]+;", "package " + newPackagePath + ";");

                        String sourceImport = sourcePath.getParent().toString().split("src/main/java/")[1].replace('/','.');
                        String destinationImport = destinationPath.getParent().toString().split("src/main/java/")[1].replace('/','.');

                        String updatedContent = content.replaceAll(sourceImport, destinationImport);

                        Files.write(destination, updatedContent.getBytes());
                    }
                    else {
                        Files.copy(path, destination, StandardCopyOption.REPLACE_EXISTING);
                    }
                }
            }
        }
    }

}
