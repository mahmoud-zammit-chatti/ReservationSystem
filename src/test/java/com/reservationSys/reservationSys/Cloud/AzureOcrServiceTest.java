package com.reservationSys.reservationSys.Cloud;

import com.azure.ai.documentintelligence.DocumentIntelligenceClient;
import com.azure.ai.documentintelligence.DocumentIntelligenceClientBuilder;
import com.azure.ai.documentintelligence.models.*;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.polling.SyncPoller;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.ByteArrayOutputStream;
import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class AzureOcrServiceTest {

    @Autowired
    private AzureOcrService ocrService;

    @Value("${azure.storage.connection-string}")
    private String storageConnectionString;

    @Value("${azure.documentintelligence.endpoint}")
    private String diEndpoint;

    @Value("${azure.documentintelligence.key}")
    private String diKey;

    @Test
    void debugOcrOutput() {
        // Download the blob
        String imageUrl = "https://evreservationstorage.blob.core.windows.net/carte-grise-images/caretGriseTest.png";
        URI uri = URI.create(imageUrl);
        String path = uri.getPath().substring(1);
        int idx = path.indexOf('/');
        String container = path.substring(0, idx);
        String blob = path.substring(idx + 1);

        BlobServiceClient blobClient = new BlobServiceClientBuilder()
                .connectionString(storageConnectionString).buildClient();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        blobClient.getBlobContainerClient(container).getBlobClient(blob).downloadStream(out);

        // Run OCR
        DocumentIntelligenceClient diClient = new DocumentIntelligenceClientBuilder()
                .credential(new AzureKeyCredential(diKey))
                .endpoint(diEndpoint)
                .buildClient();
        SyncPoller<AnalyzeOperationDetails, AnalyzeResult> poller =
                diClient.beginAnalyzeDocument("prebuilt-read", new AnalyzeDocumentOptions(out.toByteArray()));
        AnalyzeResult result = poller.getFinalResult();

        System.out.println("===== OCR DEBUG OUTPUT =====");
        for (DocumentPage page : result.getPages()) {
            System.out.println("--- Page " + page.getPageNumber() + " ---");
            for (DocumentLine line : page.getLines()) {
                System.out.println("LINE: [" + line.getContent() + "]");
            }
        }
        System.out.println("===== END OCR DEBUG =====");
    }

    @Test
    void testExtractVin() {
        String imageUrl = "https://evreservationstorage.blob.core.windows.net/carte-grise-images/caretGriseTest.png";
        AnalyzeResult result = ocrService.getAnalyzeResult(imageUrl);
        String vin = ocrService.extractVin(result);
        System.out.println("Extracted VIN: " + vin);
        assertNotNull(vin);
        assertEquals("1HGBH41JXMN109186", vin);
    }
}