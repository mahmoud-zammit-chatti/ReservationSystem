package com.reservationSys.reservationSys.Domain.notification;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class NotificationLog {
    @Id
    @GeneratedValue(strategy= GenerationType.UUID)
    private UUID id;

    private UUID reservationId;

    private NotificationType type;

    private Channel channel;

    private Boolean isSent;

    private LocalDateTime sentAt;

}
