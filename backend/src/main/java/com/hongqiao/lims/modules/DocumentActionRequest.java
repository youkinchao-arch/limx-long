package com.hongqiao.lims.modules;

import java.time.LocalDate;

/** Payload for a document workflow transition (submit/approve/reject/obsolete). */
public record DocumentActionRequest(String comment, LocalDate effectiveDate) {
}
