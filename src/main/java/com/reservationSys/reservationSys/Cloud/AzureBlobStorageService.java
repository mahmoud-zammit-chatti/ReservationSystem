package com.reservationSys.reservationSys.Cloud;


import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.util.UUID;

@Component
public class AzureBlobStorageService {

    @Value("${azure.storage.connection-string}")
    private String connectionString;

    @Value("${azure.storage.container-name}")
    private String containerName;

    private BlobServiceClient blobServiceClient;


    @PostConstruct
    public void init() {
        blobServiceClient = new BlobServiceClientBuilder()
                .connectionString(connectionString)
                .buildClient();
    }

    public String uploadImage(MultipartFile file, UUID userId) {
        String blobName = "carteGrise_"+userId + "_"+ Instant.now().toEpochMilli()+ "_" + file.getOriginalFilename();
        // Code to upload the file to Azure Blob Storage using the connection string and container name
        // You can use Azure Storage SDK for Java to perform the upload operation

        try {
            this.blobServiceClient.getBlobContainerClient(containerName)
                    .getBlobClient(blobName)
                    .upload(file.getInputStream(), file.getSize(), true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return this.blobServiceClient.getBlobContainerClient(containerName)
                .getBlobClient(blobName)
                .getBlobUrl();
    }


}
