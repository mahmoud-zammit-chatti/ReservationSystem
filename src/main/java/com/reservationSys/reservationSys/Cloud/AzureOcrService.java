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
import java.util.Locale;
import java.util.Optional;
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

    private static final Pattern VIN_PATTERN = Pattern.compile("[A-HJ-NPR-Z0-9]{17}");

    private static final Pattern PLATE_STANDARD = Pattern.compile("(\\d{2,3})تونس(\\d{4})");
    private static final Pattern PLATE_STANDARD_REVERSED = Pattern.compile("تونس(\\d{4})(\\d{2,3})");
    private static final Pattern PLATE_NET = Pattern.compile("(\\d{5,6})نت");
    private static final Pattern PLATE_NET_REVERSED = Pattern.compile("نت(\\d{5,6})");
    private static final Pattern NET_DIGITS = Pattern.compile("(\\d{5,6})");
    private static final Pattern PLATE_LABEL = Pattern.compile("(PLAQUE|PLATE|IMMATRICULATION|IMMAT|NUMERO|NUMBER|رقم|لوحة)", Pattern.CASE_INSENSITIVE);

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

    public String extractVin(AnalyzeResult result){
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

    public String extractPlateNumber(AnalyzeResult result){
        StringBuilder mergedNormalized = new StringBuilder();

        for (DocumentPage page : result.getPages()) {
            for (DocumentLine line : page.getLines()) {
                String rawLine = line.getContent();
                String normalizedLine = normalizePlateText(rawLine);

                if (!normalizedLine.isEmpty()) {
                    Optional<String> directMatch = findPlateInNormalizedText(normalizedLine);
                    if (directMatch.isPresent()) {
                        return directMatch.get();
                    }
                    mergedNormalized.append(normalizedLine);
                }

                Optional<String> contextualNet = findContextualNetPlate(rawLine);
                if (contextualNet.isPresent()) {
                    return contextualNet.get();
                }
            }
        }

        // Fallback for OCR that splits the plate across multiple lines.
        return findPlateInNormalizedText(mergedNormalized.toString()).orElse(null);
    }

    public AnalyzeResult getAnalyzeResult(String imageUrl) {
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

    private String normalizePlateText(String rawText) {
        if (rawText == null) {
            return "";
        }

        String normalized = toAsciiDigits(rawText)
                .toUpperCase(Locale.ROOT)
                .replaceAll("[\\s\\-_/.:]", "");

        // Keep only Arabic letters and digits so label noise does not break matching.
        return normalized.replaceAll("[^\\p{IsArabic}0-9]", "");
    }

    private Optional<String> findPlateInNormalizedText(String normalizedText) {
        if (normalizedText == null || normalizedText.isEmpty()) {
            return Optional.empty();
        }

        Matcher standard = PLATE_STANDARD.matcher(normalizedText);
        if (standard.find()) {
            return Optional.of(standard.group(1) + "تونس" + standard.group(2));
        }

        Matcher standardReversed = PLATE_STANDARD_REVERSED.matcher(normalizedText);
        if (standardReversed.find()) {
            return Optional.of(standardReversed.group(2) + "تونس" + standardReversed.group(1));
        }

        Matcher net = PLATE_NET.matcher(normalizedText);
        if (net.find()) {
            return Optional.of(net.group(1) + "نت");
        }

        Matcher netReversed = PLATE_NET_REVERSED.matcher(normalizedText);
        if (netReversed.find()) {
            return Optional.of(netReversed.group(1) + "نت");
        }

        return Optional.empty();
    }

    private Optional<String> findContextualNetPlate(String rawLine) {
        if (rawLine == null || !PLATE_LABEL.matcher(rawLine).find()) {
            return Optional.empty();
        }

        String asciiDigitsOnlyLine = toAsciiDigits(rawLine);
        Matcher netDigits = NET_DIGITS.matcher(asciiDigitsOnlyLine);
        if (netDigits.find()) {
            return Optional.of(netDigits.group(1) + "نت");
        }

        return Optional.empty();
    }

    private String toAsciiDigits(String input) {
        return input
                .replace('٠', '0').replace('١', '1').replace('٢', '2').replace('٣', '3').replace('٤', '4')
                .replace('٥', '5').replace('٦', '6').replace('٧', '7').replace('٨', '8').replace('٩', '9')
                .replace('۰', '0').replace('۱', '1').replace('۲', '2').replace('۳', '3').replace('۴', '4')
                .replace('۵', '5').replace('۶', '6').replace('۷', '7').replace('۸', '8').replace('۹', '9');
    }

}
