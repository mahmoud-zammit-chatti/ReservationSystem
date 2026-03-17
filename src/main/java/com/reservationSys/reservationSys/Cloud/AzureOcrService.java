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

import javax.imageio.ImageIO;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
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
    private static final String ARABIC_GAP = "[\\p{IsArabic}]{0,24}";

    private static final Pattern PLATE_STANDARD = Pattern.compile("(\\d{2,3})تونس(\\d{3,4})");
    private static final Pattern PLATE_STANDARD_REVERSED = Pattern.compile("تونس(\\d{3,4})(\\d{2,3})");
    private static final Pattern PLATE_STANDARD_WITH_GAP = Pattern.compile("(\\d{2,3})" + ARABIC_GAP + "تونس" + ARABIC_GAP + "(\\d{3,4})");
    private static final Pattern PLATE_STANDARD_REVERSED_WITH_GAP = Pattern.compile("تونس" + ARABIC_GAP + "(\\d{3,4})" + ARABIC_GAP + "(\\d{2,3})");
    private static final Pattern PLATE_VERTICAL_WITH_GAP = Pattern.compile("(\\d{3,4})" + ARABIC_GAP + "(\\d{2,3})" + ARABIC_GAP + "تونس");
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
        if (result == null || result.getPages() == null) {
            return null;
        }

        for (DocumentPage page : result.getPages()) {
            for (DocumentLine line : page.getLines()) {
                String cleaned = normalizeVinCandidate(line.getContent());
                Matcher matcher = VIN_PATTERN.matcher(cleaned);

                if (matcher.find()) {
                    return matcher.group();
                }
            }
        }
        return null;
    }

    public String extractPlateNumber(AnalyzeResult result){
        if (result == null || result.getPages() == null) {
            return null;
        }

        for (DocumentPage page : result.getPages()) {
            List<DocumentLine> lines = page.getLines();
            if (lines == null || lines.isEmpty()) {
                continue;
            }

            Optional<String> labelAnchoredMatch = findPlateNearLabel(lines);
            if (labelAnchoredMatch.isPresent()) {
                return labelAnchoredMatch.get();
            }

            for (DocumentLine line : lines) {
                String rawLine = line.getContent();
                String normalizedLine = normalizePlateText(rawLine);

                if (!normalizedLine.isEmpty()) {
                    Optional<String> directMatch = findPlateInNormalizedText(normalizedLine);
                    if (directMatch.isPresent()) {
                        return directMatch.get();
                    }
                }

                Optional<String> contextualNet = findContextualNetPlate(rawLine);
                if (contextualNet.isPresent()) {
                    return contextualNet.get();
                }
            }

            Optional<String> windowMatch = findPlateAcrossAdjacentLines(lines);
            if (windowMatch.isPresent()) {
                return windowMatch.get();
            }
        }

        return null;
    }

    public AnalyzeResult getAnalyzeResult(String imageUrl) {
        byte[] fileBytes = downloadBlob(imageUrl);
        List<byte[]> candidates = buildRotationCandidates(fileBytes);

        AnalyzeResult bestResult = null;
        int bestScore = -1;

        for (byte[] candidate : candidates) {
            AnalyzeResult result = analyzeDocument(candidate);
            int score = 0;

            if (extractVin(result) != null) {
                score++;
            }
            if (extractPlateNumber(result) != null) {
                score++;
            }

            if (score > bestScore) {
                bestScore = score;
                bestResult = result;
            }

            if (score == 2) {
                return result;
            }
        }

        return bestResult;
    }

    private AnalyzeResult analyzeDocument(byte[] fileBytes) {
        AnalyzeDocumentOptions options = new AnalyzeDocumentOptions(fileBytes);
        SyncPoller<AnalyzeOperationDetails, AnalyzeResult> poller =
                client.beginAnalyzeDocument("prebuilt-read", options);
        // blocks until the operation is complete and returns the result
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

        Matcher standardWithGap = PLATE_STANDARD_WITH_GAP.matcher(normalizedText);
        if (standardWithGap.find()) {
            return Optional.of(standardWithGap.group(1) + "تونس" + standardWithGap.group(2));
        }

        Matcher standardReversedWithGap = PLATE_STANDARD_REVERSED_WITH_GAP.matcher(normalizedText);
        if (standardReversedWithGap.find()) {
            return Optional.of(standardReversedWithGap.group(2) + "تونس" + standardReversedWithGap.group(1));
        }

        Matcher verticalWithGap = PLATE_VERTICAL_WITH_GAP.matcher(normalizedText);
        if (verticalWithGap.find()) {
            return Optional.of(verticalWithGap.group(2) + "تونس" + verticalWithGap.group(1));
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

    private Optional<String> findPlateAcrossAdjacentLines(List<DocumentLine> lines) {
        // Try short windows so split vertical plate parts can be recombined without scanning unrelated fields.
        for (int start = 0; start < lines.size(); start++) {
            StringBuilder window = new StringBuilder();
            for (int end = start; end < Math.min(start + 4, lines.size()); end++) {
                String normalized = normalizePlateText(lines.get(end).getContent());
                if (normalized.isEmpty()) {
                    continue;
                }

                window.append(normalized);
                Optional<String> match = findPlateInNormalizedText(window.toString());
                if (match.isPresent()) {
                    return match;
                }
            }
        }

        return Optional.empty();
    }

    private Optional<String> findPlateNearLabel(List<DocumentLine> lines) {
        for (int i = 0; i < lines.size(); i++) {
            String content = lines.get(i).getContent();
            if (content == null || !PLATE_LABEL.matcher(content).find()) {
                continue;
            }

            StringBuilder localWindow = new StringBuilder();
            int start = Math.max(0, i - 2);
            int end = Math.min(lines.size() - 1, i + 4);

            for (int j = start; j <= end; j++) {
                localWindow.append(normalizePlateText(lines.get(j).getContent()));
            }

            Optional<String> match = findPlateInNormalizedText(localWindow.toString());
            if (match.isPresent()) {
                return match;
            }
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

    private List<byte[]> buildRotationCandidates(byte[] originalBytes) {
        List<byte[]> candidates = new ArrayList<>();
        candidates.add(originalBytes);

        addRotatedCandidate(candidates, originalBytes, 90);
        addRotatedCandidate(candidates, originalBytes, 270);
        addRotatedCandidate(candidates, originalBytes, 180);

        return candidates;
    }

    private void addRotatedCandidate(List<byte[]> candidates, byte[] originalBytes, int angle) {
        rotateImageBytes(originalBytes, angle).ifPresent(candidates::add);
    }

    private Optional<byte[]> rotateImageBytes(byte[] inputBytes, int angleDegrees) {
        try {
            BufferedImage source = ImageIO.read(new ByteArrayInputStream(inputBytes));
            if (source == null) {
                return Optional.empty();
            }

            BufferedImage rotated = rotateByRightAngle(source, angleDegrees);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(rotated, "png", outputStream);
            return Optional.of(outputStream.toByteArray());
        } catch (Exception ex) {
            return Optional.empty();
        }
    }

    private BufferedImage rotateByRightAngle(BufferedImage source, int angleDegrees) {
        int normalized = Math.floorMod(angleDegrees, 360);
        if (normalized == 0) {
            return source;
        }

        int sourceWidth = source.getWidth();
        int sourceHeight = source.getHeight();

        int targetWidth = normalized == 180 ? sourceWidth : sourceHeight;
        int targetHeight = normalized == 180 ? sourceHeight : sourceWidth;

        BufferedImage rotated = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);

        AffineTransform transform = new AffineTransform();
        if (normalized == 90) {
            transform.translate(sourceHeight, 0);
            transform.rotate(Math.toRadians(90));
        } else if (normalized == 180) {
            transform.translate(sourceWidth, sourceHeight);
            transform.rotate(Math.toRadians(180));
        } else if (normalized == 270) {
            transform.translate(0, sourceWidth);
            transform.rotate(Math.toRadians(270));
        }

        AffineTransformOp operation = new AffineTransformOp(transform, AffineTransformOp.TYPE_BILINEAR);
        operation.filter(source, rotated);
        return rotated;
    }

    private String normalizeVinCandidate(String rawText) {
        if (rawText == null) {
            return "";
        }

        String normalized = toAsciiDigits(rawText)
                .toUpperCase(Locale.ROOT)
                .replace('*', 'V')
                .replaceAll("[\\s\\-_/.:]", "");

        // Keep VIN-relevant characters only after conservative OCR correction.
        return normalized.replaceAll("[^A-Z0-9]", "");
    }

    private String toAsciiDigits(String input) {
        return input
                .replace('٠', '0').replace('١', '1').replace('٢', '2').replace('٣', '3').replace('٤', '4')
                .replace('٥', '5').replace('٦', '6').replace('٧', '7').replace('٨', '8').replace('٩', '9')
                .replace('۰', '0').replace('۱', '1').replace('۲', '2').replace('۳', '3').replace('۴', '4')
                .replace('۵', '5').replace('۶', '6').replace('۷', '7').replace('۸', '8').replace('۹', '9');
    }

}
