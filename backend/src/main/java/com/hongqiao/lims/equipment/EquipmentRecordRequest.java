package com.hongqiao.lims.equipment;

import java.time.LocalDate;

/**
 * Payload for recording an equipment calibration/maintenance event.
 * {@code password} re-authenticates the actor to produce the e-signature hash.
 */
public record EquipmentRecordRequest(
        String recordType,
        String result,
        LocalDate performedDate,
        String provider,
        String certificateNo,
        LocalDate nextDueDate,
        Integer cycleDays,
        String notes,
        String password) {
}
