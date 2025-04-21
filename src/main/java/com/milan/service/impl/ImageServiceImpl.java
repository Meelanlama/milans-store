package com.milan.service.impl;

import com.milan.service.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ImageServiceImpl implements ImageService {

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(ImageServiceImpl.class);

    @Override
    public String uploadImage(MultipartFile file, String path) throws IOException {

        String originalFilename = file.getOriginalFilename();
        logger.info("Filename: {}" , originalFilename);

        if (originalFilename == null || !originalFilename.contains(".")) {
            logger.error("Invalid file name: {}", originalFilename);
            throw new IllegalArgumentException("Invalid file name");
        }

        // Generate a random UUID for unique naming
        String filename = UUID.randomUUID().toString();
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));

        List<String> allowedExtensions = List.of(".png", ".jpg", ".jpeg");

        if (allowedExtensions.stream().noneMatch(extension::equalsIgnoreCase)) {
            throw new IllegalArgumentException("File format not allowed: " + extension);
        }

        // Create full file path
        String fileNameWithExtension = filename + extension;

        String fullPathWithFilename = path+fileNameWithExtension;

        // Create directory if it doesn't exist
        File folder = new File(path);
        if (!folder.exists()) {
            folder.mkdirs();
        }

        // Save the uploaded file
        Files.copy(file.getInputStream(), Paths.get(fullPathWithFilename));

        return fileNameWithExtension;
    }

    //Retrieves an image resource as an InputStream.
    @Override
    public InputStream getResource(String path, String name) throws FileNotFoundException {
        String fullPath = path + File.separator + name;
        InputStream ios = new FileInputStream(fullPath);
        return ios;
    }

}
