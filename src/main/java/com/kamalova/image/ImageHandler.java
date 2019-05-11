package com.kamalova.image;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifDirectoryBase;
import com.drew.metadata.file.FileTypeDirectory;
import com.drew.metadata.mp4.Mp4Directory;

import java.io.File;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;


public class ImageHandler {

    private static final int imageDatetimeOriginal = ExifDirectoryBase.TAG_DATETIME_ORIGINAL;
    private static final int tagMovies = Mp4Directory.TAG_CREATION_TIME;

    public void sortImages(String path) {
        File directory = new File(path);
        if (directory.exists() && directory.isDirectory()) {
            for (File file: directory.listFiles()) {
                try {
                    Metadata metadata = ImageMetadataReader.readMetadata(file);
                    String fileExtension = getFileExtension(metadata);
                    Date date = getCreationDate(metadata);
                    if (date != null) {
                        changeFile(file, date, fileExtension);
                    } else {
                        System.out.println(metadata.toString());
                    }
                } catch (Exception e) {
                    System.out.println("Error with file: " + file.getName());
                    e.printStackTrace();
                    System.out.println();
                }

            }
        }
    }

    private Date getCreationDate(Metadata data) {
        for(Directory metaData : data.getDirectories()) {
            if (metaData.getDate(imageDatetimeOriginal) != null) {
                return metaData.getDate(imageDatetimeOriginal);
            }
            if (metaData.getDate(tagMovies) != null) {
                return metaData.getDate(tagMovies);
            }
        }
        throw new IllegalArgumentException("No Image or Video file in data: " + data);
    }

    private String getFileExtension(Metadata metadata) throws IllegalAccessException {
        if (metadata.containsDirectoryOfType(FileTypeDirectory.class)) {
            FileTypeDirectory metaData = metadata.getFirstDirectoryOfType(FileTypeDirectory.class);
            return metaData.getString(FileTypeDirectory.TAG_EXPECTED_FILE_NAME_EXTENSION);
        } else {
            throw new IllegalAccessException("Can't parse correct tags from metaData for" +
                    "file with metaData: " + metadata.toString());
        }
    }

    private void changeFile(File file, Date date, String extension) {
        String newPath = getNewPath(file, date);
        File renamedImage = renameImage(newPath, extension);
        file.renameTo(renamedImage);
    }

    private String getNewPath(File file, Date creationDate) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd-HHmmss");
        String newName = simpleDateFormat.format(creationDate);
        return file.getAbsolutePath().replace(file.getName(), newName);
    }

    private String getFileNameWithExtension(String oldName, String extension) {
        return oldName.concat("." + extension);
    }

    private File renameImage(String oldName, String extension) {
        File uniqueFile = new File(getFileNameWithExtension(oldName, extension));
        int suffix = 0;
        while (uniqueFile.exists()) {
            String uniqueName = createUniqueFileName(oldName, suffix);
            uniqueFile = new File(getFileNameWithExtension(uniqueName, extension));
            suffix++;
        }
        return uniqueFile;
    }

    private String createUniqueFileName(String oldName, int suffix) {
        return oldName.concat("_" + suffix);
    }

    public static void main(String[] args) {
        URL main = ImageHandler.class.getResource("ImageHandler.class");
        if (!"file".equalsIgnoreCase(main.getProtocol()))
            throw new IllegalStateException("Main class is not stored in a file.");
        File path = new File(main.getPath());
        System.out.println(path.getAbsolutePath());
//        ImageHandler imageHandler = new ImageHandler();
//        imageHandler.sortImages("C:\\Users\\Irisha\\Pictures\\DataSpoiled");
    }
}
