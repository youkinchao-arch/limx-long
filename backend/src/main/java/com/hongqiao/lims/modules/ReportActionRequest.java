package com.hongqiao.lims.modules;

import java.time.LocalDate;

/** Payload for a report workflow transition (submit/approve/reject/sign/issue). */
public record ReportActionRequest(String comment, String password, String meaning, LocalDate issueDate) {
}
