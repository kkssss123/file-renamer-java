import fileutils.RenameImageFilesV3;

public class Main {

    public static void main(String[] args) {

        String baseDir = "./images";
        String destDir = "./output";

        int startNum = 1;
        if (args.length > 0) {
            try {
                startNum = Integer.parseInt(args[0].trim());
                if (startNum < 1)
                    throw new NumberFormatException();
            } catch (NumberFormatException e) {
                System.err.println("Warning: Invalid start number format. Defaulting to 1.");
            }
        }

        RenameImageFilesV3 renameImageFilesV3 = new RenameImageFilesV3(baseDir, destDir, startNum);
        renameImageFilesV3.execute();
    }
}
