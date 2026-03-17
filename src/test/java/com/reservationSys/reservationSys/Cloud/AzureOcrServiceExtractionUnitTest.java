package com.reservationSys.reservationSys.Cloud;

import com.azure.ai.documentintelligence.models.AnalyzeResult;
import com.azure.ai.documentintelligence.models.DocumentLine;
import com.azure.ai.documentintelligence.models.DocumentPage;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AzureOcrServiceExtractionUnitTest {

    @Test
    void extractVinCorrectsCorruptedLeadingCharacter() {
        AzureOcrService service = new AzureOcrService();

        AnalyzeResult result = mock(AnalyzeResult.class);
        DocumentPage page = mock(DocumentPage.class);
        DocumentLine line = mock(DocumentLine.class);

        when(line.getContent()).thenReturn("*HGCM82633A004352");
        when(page.getLines()).thenReturn(List.of(line));
        when(result.getPages()).thenReturn(List.of(page));

        String extracted = service.extractVin(result);

        assertEquals("VHGCM82633A004352", extracted);
    }

    @Test
    void extractVinIgnoresSpacingAndSeparators() {
        AzureOcrService service = new AzureOcrService();

        AnalyzeResult result = mock(AnalyzeResult.class);
        DocumentPage page = mock(DocumentPage.class);
        DocumentLine line = mock(DocumentLine.class);

        when(line.getContent()).thenReturn("VHGCM 82633-A004352");
        when(page.getLines()).thenReturn(List.of(line));
        when(result.getPages()).thenReturn(List.of(page));

        String extracted = service.extractVin(result);

        assertEquals("VHGCM82633A004352", extracted);
    }

    @Test
    void extractPlateNumberHandlesSplitVerticalStandardPlate() {
        AzureOcrService service = new AzureOcrService();

        AnalyzeResult result = mock(AnalyzeResult.class);
        DocumentPage page = mock(DocumentPage.class);

        DocumentLine l1 = mock(DocumentLine.class);
        DocumentLine l2 = mock(DocumentLine.class);
        DocumentLine l3 = mock(DocumentLine.class);
        DocumentLine l4 = mock(DocumentLine.class);

        when(l1.getContent()).thenReturn("5022");
        when(l2.getContent()).thenReturn("رقم التسجيل");
        when(l3.getContent()).thenReturn("139");
        when(l4.getContent()).thenReturn("تونس");

        when(page.getLines()).thenReturn(List.of(l1, l2, l3, l4));
        when(result.getPages()).thenReturn(List.of(page));

        String extracted = service.extractPlateNumber(result);

        assertEquals("139تونس5022", extracted);
    }

    @Test
    void extractPlateNumberPrefersLabelAnchoredSplitPlateOverUnrelatedDigits() {
        AzureOcrService service = new AzureOcrService();

        AnalyzeResult result = mock(AnalyzeResult.class);
        DocumentPage page = mock(DocumentPage.class);

        DocumentLine l1 = mock(DocumentLine.class);
        DocumentLine l2 = mock(DocumentLine.class);
        DocumentLine l3 = mock(DocumentLine.class);
        DocumentLine l4 = mock(DocumentLine.class);
        DocumentLine l5 = mock(DocumentLine.class);
        DocumentLine l6 = mock(DocumentLine.class);

        when(l1.getContent()).thenReturn("Date: 06/14/2009");
        when(l2.getContent()).thenReturn("765");
        when(l3.getContent()).thenReturn("رقم التسجيل");
        when(l4.getContent()).thenReturn("190");
        when(l5.getContent()).thenReturn("تونس");
        when(l6.getContent()).thenReturn("PEUGEOT");

        when(page.getLines()).thenReturn(List.of(l1, l2, l3, l4, l5, l6));
        when(result.getPages()).thenReturn(List.of(page));

        String extracted = service.extractPlateNumber(result);

        assertEquals("190تونس765", extracted);
    }
}

