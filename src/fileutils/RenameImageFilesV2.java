package fileutils;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

public class RenameImageFilesV2 {
    private final String baseDir;
    private final String destDir;

    public RenameImageFilesV2(String baseDir, String destDir) {
        this.baseDir = baseDir;
        this.destDir = destDir;
    }

    public void execute() {
        try {
            Files.createDirectories(Paths.get(destDir));
            Files.walkFileTree(Paths.get(baseDir), new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {

                    Path relativeDir = Paths.get(baseDir).relativize(dir);
                    Path targetDir = Paths.get(destDir).resolve(relativeDir);
                    Files.createDirectories(targetDir);
                    renameAndCopyFilesInDirectory(dir, targetDir);

                    return FileVisitResult.CONTINUE;
                }
            });

            System.out.println("All image files renamed and copied successfully!");
        } catch (IOException e) {
            System.err.println("Error processing directories: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void renameAndCopyFilesInDirectory(Path sourceDir, Path targetDir) throws IOException {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(sourceDir, path -> {
            String fileName = path.getFileName().toString().toLowerCase();
            return fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") ||
                    fileName.endsWith(".webp") || fileName.endsWith(".png") ||
                    fileName.endsWith(".gif") || fileName.endsWith(".mp4") ||
                    fileName.endsWith(".mov");
        })) {
            List<Path> files = new ArrayList<>();
            stream.forEach(files::add);

            files.sort(Comparator.comparing(path -> path.getFileName().toString()));

            int startNum = 1;
            for (Path file : files) {
                String fileName = file.getFileName().toString();
                String extension = getFileExtension(fileName);

                String newFileName = getNewFileName(startNum++, extension);

                Path newFilePath = targetDir.resolve(newFileName);
                Files.copy(file, newFilePath, StandardCopyOption.REPLACE_EXISTING);
                System.out.println("Copied and renamed: " + file.getFileName() + " -> " + newFileName);
            }
        }
    }

    private static String getNewFileName(int startNum, String extension) {
        StringBuilder newFileNameBuilder = new StringBuilder(String.valueOf(startNum));

        if (newFileNameBuilder.length() < 3) {
            while (newFileNameBuilder.length() < 3) {
                newFileNameBuilder.insert(0, '0');
            }
        } else if (newFileNameBuilder.length() > 3) {
            newFileNameBuilder.delete(0, newFileNameBuilder.length() - 3);
        }

        return newFileNameBuilder + extension;
    }

    private static String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        return (lastDotIndex != -1) ? fileName.substring(lastDotIndex) : "";
    }
}
