package org.example.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.stream.Stream;

public class StarterUtil {

    public static void copySourceModuleToLocalPath(String templatePath, String source, Path destinationPath) throws IOException {
        Path sourcePath = Paths.get(templatePath, source);
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

                        String sourceImport = path.getParent().toString().split("src/main/java/")[1].replace('/','.');
                        String destinationImport = destination.getParent().toString().split("src/main/java/")[1].replace('/','.');

                        String updatedContent = updateContent(content, sourceImport, destinationImport);

                        Files.write(destination, updatedContent.getBytes());
                    }
                    else {
                        Files.copy(path, destination, StandardCopyOption.REPLACE_EXISTING);
                    }
                }
            }
        }
    }

    /**
     * The function updates all package and import statements of a .java class;
     * @param content - content to update
     * @param source - the source classpath
     * @param target - the target classpath
     * @return - updated class content with all packages and import replaced.
     */
    public static String updateContent(String content, String source, String target) {
        String updatedContent = content.replaceAll(source, target);

        while(true) {
            int srcLastIdx = source.lastIndexOf('.');
            int targetLastIdx = target.lastIndexOf('.');

            if(source.substring(srcLastIdx+1).equals(target.substring(targetLastIdx+1))) {
                source = source.substring(0, srcLastIdx);
                target = target.substring(0, targetLastIdx);
                updatedContent = updatedContent.replaceAll(source, target);
            }
            else {
                break;
            }
        }

        return updatedContent;
    }

}
