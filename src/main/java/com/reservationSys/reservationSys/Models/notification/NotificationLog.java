package com.reservationSys.reservationSys.Models.notification;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
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

    @Enumerated(EnumType.STRING)
    private NotificationType type;

    @Enumerated(EnumType.STRING)
    private Channel channel;

    private Boolean isSent;

    private Instant sentAt;

}
