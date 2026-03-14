package com.reservationSys.reservationSys.Cloud;

import com.azure.ai.documentintelligence.DocumentIntelligenceClient;
import com.azure.ai.documentintelligence.DocumentIntelligenceClientBuilder;
import com.azure.ai.documentintelligence.models.AnalyzeDocumentOptions;
import com.azure.ai.documentintelligence.models.AnalyzeOperationDetails;
import com.azure.ai.documentintelligence.models.AnalyzeResult;
import com.azure.ai.documentintelligence.models.DocumentLine;
import com.azure.ai.documentintelligence.models.DocumentPage;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.polling.SyncPoller;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class AzureOcrService {

    @Value("${azure.documentintelligence.endpoint}")
    private String endpoint;

    @Value("${azure.documentintelligence.key}")
    private String key;

    @Value("${azure.storage.connection-string}")
    private String storageConnectionString;

    Pattern VIN_PATTERN = Pattern.compile("[A-HJ-NPR-Z0-9]{17}");


    Pattern PLATE_PATTERN = Pattern.compile(
            "\\d{2,3}\\s*تونس\\s*\\d{4}|\\d{5,6}\\s*ن\\s*ت"
    );

    private DocumentIntelligenceClient client;
    private BlobServiceClient blobServiceClient;

    @PostConstruct
    public void init() {
        client = new DocumentIntelligenceClientBuilder()
                .credential(new AzureKeyCredential(key))
                .endpoint(endpoint)
                .buildClient();
        blobServiceClient = new BlobServiceClientBuilder()
                .connectionString(storageConnectionString)
                .buildClient();
    }

    public String extractVin(String imageUrl){
        AnalyzeResult result = getAnalyzeResult(imageUrl);
        for (DocumentPage page : result.getPages()) {
            for (DocumentLine line : page.getLines()) {
                // Remove spaces and dashes that OCR may insert within the VIN
                String cleaned = line.getContent().replaceAll("[\\s\\-]", "");
                Matcher matcher = VIN_PATTERN.matcher(cleaned);

                if (matcher.find()) {
                    return matcher.group();
                }
            }
        }
        return null;
    }

    public String extractPlateNumber(String imageUrl){
        AnalyzeResult result = getAnalyzeResult(imageUrl);
        for(DocumentPage page : result.getPages()){
            for(DocumentLine line : page.getLines()){
                String cleaned = line.getContent().replaceAll("[\\s\\-]", "");
                Matcher matcher = PLATE_PATTERN.matcher(cleaned);

                if(matcher.find()){
                    return matcher.group();
                }
            }
        }

        return null;
    }

    private AnalyzeResult getAnalyzeResult(String imageUrl) {
        byte[] fileBytes = downloadBlob(imageUrl);
        AnalyzeDocumentOptions options = new AnalyzeDocumentOptions(fileBytes);
        SyncPoller<AnalyzeOperationDetails, AnalyzeResult> poller =
                client.beginAnalyzeDocument("prebuilt-read", options);
        //blocks until the operation is complete and returns the result
        return poller.getFinalResult();
    }

    /**
     * Downloads a blob from Azure Blob Storage using the authenticated BlobServiceClient.
     * Parses the container name and blob name from the given blob URL.
     */
    private byte[] downloadBlob(String blobUrl) {
        URI uri = URI.create(blobUrl);
        // Path is like /container-name/blob-name
        String path = uri.getPath();
        // Remove leading slash and split into container + blob
        String pathWithoutLeadingSlash = path.substring(1);
        int slashIndex = pathWithoutLeadingSlash.indexOf('/');
        String containerName = pathWithoutLeadingSlash.substring(0, slashIndex);
        String blobName = pathWithoutLeadingSlash.substring(slashIndex + 1);

        BlobClient blobClient = blobServiceClient
                .getBlobContainerClient(containerName)
                .getBlobClient(blobName);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        blobClient.downloadStream(outputStream);
        return outputStream.toByteArray();
    }

}
