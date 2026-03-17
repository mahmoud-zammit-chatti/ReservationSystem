package com.reservationSys.reservationSys.Services.Car;

import com.azure.ai.documentintelligence.models.AnalyzeResult;
import com.reservationSys.reservationSys.Cloud.AzureOcrService;
import com.reservationSys.reservationSys.Domain.car.Car;
import com.reservationSys.reservationSys.Domain.car.CarStatus;
import com.reservationSys.reservationSys.Repositories.CarRepo;
import com.reservationSys.reservationSys.exceptions.RessourceNotFound;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;


import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
public class CarVerificationService {
    private final CarRepo carRepo;
    private final AzureOcrService azureOcrService;
    private final CarUpdateService carUpdateService;


    public CarVerificationService(CarRepo carRepo, AzureOcrService azureOcrService, CarUpdateService carUpdateService) {
        this.carRepo = carRepo;
        this.azureOcrService = azureOcrService;

        this.carUpdateService = carUpdateService;
    }

    @Async
    public void verifyCar(UUID carId) {
        Car carToVerify = carRepo.findById(carId).orElseThrow(()-> new RessourceNotFound("Car with id "+carId+" not found"));

        AnalyzeResult result = azureOcrService.getAnalyzeResult(carToVerify.getCarteGriseUrl());

        String extractedVin = azureOcrService.extractVin(result);
        String extractedPlate = azureOcrService.extractPlateNumber(result);

        String expectedPlate = normalizePlate(carToVerify.getPlateNumber());
        String normalizedExtractedPlate = normalizePlate(extractedPlate);

        if(extractedVin==null || extractedPlate==null){
            log.warn("OCR failed to extract VIN or plate for car {}", carId);
        }

        if(Objects.equals(extractedVin, carToVerify.getChassisNumber()) && Objects.equals(normalizedExtractedPlate, expectedPlate))  {
            log.info("Car with id {} verified successfully.", carId);
            //send in app notification here as well
            carUpdateService.updateCarStatus(carId,CarStatus.VERIFIED);
        }else{
            log.warn("Car with id {} verification failed. Extracted VIN: {}, Extracted Plate: {}", carId, extractedVin, extractedPlate);
            //sending in app notification or email notification in the future and adding that to the notificationLog
        }
    }

    private String normalizePlate(String plateValue) {
        if (plateValue == null) {
            return null;
        }

        String normalized = plateValue
                .replace('٠','0').replace('١','1').replace('٢','2').replace('٣','3').replace('٤','4')
                .replace('٥','5').replace('٦','6').replace('٧','7').replace('٨','8').replace('٩','9')
                .replace('۰','0').replace('۱','1').replace('۲','2').replace('۳','3').replace('۴','4')
                .replace('۵','5').replace('۶','6').replace('۷','7').replace('۸','8').replace('۹','9');

        return normalized.replaceAll("[^\\p{IsArabic}0-9]", "");
    }

}
