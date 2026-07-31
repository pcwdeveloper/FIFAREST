package com.fifa.fifarest.service;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.BlobHttpHeaders;
import com.azure.storage.blob.sas.BlobSasPermission;
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.time.OffsetDateTime;

@Service
public class AzureBlobStorageService {

    private static final long SAS_EXPIRY_HOURS = 24;

    private final BlobContainerClient containerClient;

    public AzureBlobStorageService(@Value("${app.azure.storage.connection-string}") String connectionString,
                                    @Value("${app.azure.storage.container-name}") String containerName) {
        BlobServiceClient serviceClient = new BlobServiceClientBuilder()
                .connectionString(connectionString)
                .buildClient();
        this.containerClient = serviceClient.getBlobContainerClient(containerName);
    }

    /** Uploads the file and returns the bare blob name to persist — not a URL, since the
     *  account doesn't allow public access and any URL needs a freshly-signed SAS token. */
    public String upload(String blobName, MultipartFile file) {
        try (InputStream inputStream = file.getInputStream()) {
            BlobClient blobClient = containerClient.getBlobClient(blobName);
            blobClient.upload(inputStream, file.getSize(), true);
            blobClient.setHttpHeaders(new BlobHttpHeaders().setContentType(file.getContentType()));
            return blobName;
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to upload file to Azure Blob Storage", e);
        }
    }

    /** Mints a short-lived read-only SAS URL for the blob — regenerated on every response,
     *  since the storage account doesn't permit public/anonymous access. */
    public String generateReadUrl(String blobName) {
        if (blobName == null) {
            return null;
        }
        BlobClient blobClient = containerClient.getBlobClient(blobName);
        BlobSasPermission permission = new BlobSasPermission().setReadPermission(true);
        OffsetDateTime expiry = OffsetDateTime.now().plusHours(SAS_EXPIRY_HOURS);
        BlobServiceSasSignatureValues sasValues = new BlobServiceSasSignatureValues(expiry, permission);
        String sasToken = blobClient.generateSas(sasValues);
        // Build the URL manually rather than via blobClient.getBlobUrl(), which percent-encodes
        // any "/" inside the blob name itself (e.g. "venue-images/x.jpg" -> "venue-images%2Fx.jpg"),
        // breaking the folder-style path Azure actually expects.
        return containerClient.getBlobContainerUrl() + "/" + blobName + "?" + sasToken;
    }

    public void delete(String blobName) {
        if (blobName == null) {
            return;
        }
        containerClient.getBlobClient(blobName).deleteIfExists();
    }
}
