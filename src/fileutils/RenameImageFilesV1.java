package fileutils;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

public class RenameImageFilesV1 {
    public static void main(String[] args) {
        String baseDir = "./images";

        try {
            Files.walkFileTree(Paths.get(baseDir), new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    if (!dir.equals(Paths.get(baseDir))) {
                        renameFilesInDirectory(dir);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });

            System.out.println("All image files renamed successfully!");
        } catch (IOException e) {
            System.err.println("Error processing directories: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void renameFilesInDirectory(Path dir) throws IOException {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, path -> {
            String fileName = path.getFileName().toString().toLowerCase();
            return fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") || fileName.endsWith(".webp") || fileName.endsWith(".png");
        })) {
            List<Path> files = new ArrayList<>();
            stream.forEach(files::add);

            files.sort(Comparator.comparing(path -> path.getFileName().toString()));

            for (Path file : files) {
                String fileName = file.getFileName().toString();
                String extension = getFileExtension(fileName);

                String newFileName = fileName.substring(0, fileName.indexOf('.'));
                while (newFileName.length() != 3) {
                    newFileName = "0" + newFileName;
                }
                newFileName = newFileName + extension;
                Path newFilePath = dir.resolve(newFileName); // 새 경로 생성
                Files.move(file, newFilePath, StandardCopyOption.REPLACE_EXISTING); // 파일 이름 변경
                System.out.println("Renamed: " + file.getFileName() + " -> " + newFileName);
            }
        }
    }

    private static String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        return (lastDotIndex != -1) ? fileName.substring(lastDotIndex) : "";
    }
}
