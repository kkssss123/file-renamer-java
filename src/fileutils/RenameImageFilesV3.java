package fileutils;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.math.BigInteger;

public class RenameImageFilesV3 {
    private final String baseDir;
    private final String destDir;
    private final int startNum;

    public RenameImageFilesV3(String baseDir, String destDir) {
        this(baseDir, destDir, 1);
    }

    public RenameImageFilesV3(String baseDir, String destDir, int startNum) {
        this.baseDir = baseDir;
        this.destDir = destDir;
        this.startNum = startNum;
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

    private void renameAndCopyFilesInDirectory(Path sourceDir, Path targetDir) throws IOException {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(sourceDir, path -> {
            String fileName = path.getFileName().toString().toLowerCase();
            return fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") ||
                    fileName.endsWith(".webp") || fileName.endsWith(".png") ||
                    fileName.endsWith(".gif") || fileName.endsWith(".mp4") ||
                    fileName.endsWith(".mov");
        })) {
            List<Path> files = new ArrayList<>();
            stream.forEach(files::add);

            int totalFiles = files.size();
            if (totalFiles == 0) {
                return;
            }

            if (totalFiles >= 10000) {
                System.err.println("Error: Total file count (" + totalFiles + ") in directory " + sourceDir + " exceeds 9999. Skipping this directory.");
                return;
            }

            int padWidth = totalFiles >= 1000 ? 4 : 3;
            files.sort(RenameImageFilesV3::compareNatural);
            int currentNum = this.startNum;
            for (Path file : files) {
                String fileName = file.getFileName().toString();
                String extension = getFileExtension(fileName);

                String newFileName = getNewFileName(currentNum++, padWidth, extension);

                Path newFilePath = targetDir.resolve(newFileName); // 출력 경로 생성
                Files.copy(file, newFilePath, StandardCopyOption.REPLACE_EXISTING); // 파일 복사
                System.out.println("Copied and renamed: " + file.getFileName() + " -> " + newFileName);
            }
        }
    }

    private static int compareNatural(Path p1, Path p2) {
        String s1 = p1.getFileName().toString();
        String s2 = p2.getFileName().toString();
        
        int i = 0, j = 0;
        while (i < s1.length() && j < s2.length()) {
            char c1 = s1.charAt(i);
            char c2 = s2.charAt(j);
            
            if (Character.isDigit(c1) && Character.isDigit(c2)) {
                StringBuilder num1 = new StringBuilder();
                while (i < s1.length() && Character.isDigit(s1.charAt(i))) {
                    num1.append(s1.charAt(i++));
                }
                StringBuilder num2 = new StringBuilder();
                while (j < s2.length() && Character.isDigit(s2.charAt(j))) {
                    num2.append(s2.charAt(j++));
                }
                
                BigInteger b1 = new BigInteger(num1.toString());
                BigInteger b2 = new BigInteger(num2.toString());
                int cmp = b1.compareTo(b2);
                if (cmp != 0) return cmp;
                
                cmp = Integer.compare(num1.length(), num2.length());
                if (cmp != 0) return cmp;
            } else {
                if (c1 != c2) {
                    return Character.compare(c1, c2);
                }
                i++;
                j++;
            }
        }
        return Integer.compare(s1.length(), s2.length());
    }

    private static String getNewFileName(int startNum, int padWidth, String extension) {
        return String.format("%0" + padWidth + "d%s", startNum, extension);
    }

    private static String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        return (lastDotIndex != -1) ? fileName.substring(lastDotIndex) : "";
    }
}
