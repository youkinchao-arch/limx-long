package com.hongqiao.lims.modules;

import com.hongqiao.lims.common.ApiException;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.stereotype.Service;

/** Renders an issued report (with its signature trail) to a PDF document. */
@Service
public class ReportPdfService {

    private static final DateTimeFormatter TS =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneOffset.UTC);

    private static final Font TITLE = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Color.BLACK);
    private static final Font LABEL = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.DARK_GRAY);
    private static final Font VALUE = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.BLACK);
    private static final Font SECTION = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.BLACK);
    private static final Font SMALL = FontFactory.getFont(FontFactory.HELVETICA, 8, Color.GRAY);

    public byte[] render(Report report, List<ReportSignature> signatures) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4, 48, 48, 56, 48);
        try {
            PdfWriter.getInstance(doc, out);
            doc.open();

            Paragraph header = new Paragraph("Hongqiao Lab — Test Report", TITLE);
            header.setAlignment(Element.ALIGN_CENTER);
            header.setSpacingAfter(4);
            doc.add(header);

            Paragraph subtitle = new Paragraph("虹桥试验室 检测报告", SECTION);
            subtitle.setAlignment(Element.ALIGN_CENTER);
            subtitle.setSpacingAfter(16);
            doc.add(subtitle);

            PdfPTable meta = new PdfPTable(2);
            meta.setWidthPercentage(100);
            meta.setWidths(new float[] {1f, 2.4f});
            addRow(meta, "Report No / 报告编号", nz(report.getReportNo()));
            addRow(meta, "Title / 标题", nz(report.getTitle()));
            addRow(meta, "Customer / 客户", nz(report.getCustomer()));
            addRow(meta, "Template / 模板", nz(report.getTemplate()));
            addRow(meta, "Status / 状态", nz(report.getStatus()));
            addRow(meta, "Issue Date / 出具日期",
                    report.getIssueDate() == null ? "-" : report.getIssueDate().toString());
            meta.setSpacingAfter(16);
            doc.add(meta);

            doc.add(section("Conclusion / 结论"));
            Paragraph conclusion = new Paragraph(nz(report.getConclusion()), VALUE);
            conclusion.setSpacingAfter(16);
            doc.add(conclusion);

            doc.add(section("Approval & Signatures / 审批与签名"));
            PdfPTable sig = new PdfPTable(5);
            sig.setWidthPercentage(100);
            sig.setWidths(new float[] {1.1f, 1.2f, 1f, 1.4f, 2f});
            headerCell(sig, "Action / 动作");
            headerCell(sig, "Transition / 状态");
            headerCell(sig, "Operator / 操作人");
            headerCell(sig, "Time / 时间");
            headerCell(sig, "Meaning·Hash / 含义·摘要");
            for (ReportSignature s : signatures) {
                bodyCell(sig, nz(s.getAction()));
                bodyCell(sig, nz(s.getFromStatus()) + " \u2192 " + nz(s.getToStatus()));
                bodyCell(sig, nz(s.getActorUsername()));
                bodyCell(sig, s.getCreatedAt() == null ? "-" : TS.format(s.getCreatedAt()));
                String detail = s.getMeaning() != null ? s.getMeaning() : "";
                if (s.getSignatureHash() != null) {
                    detail = (detail.isBlank() ? "" : detail + "\n") + "#" + s.getSignatureHash();
                }
                bodyCell(sig, detail.isBlank() ? "-" : detail);
            }
            doc.add(sig);

            Paragraph footer = new Paragraph(
                    "Generated " + TS.format(Instant.now()) + " UTC — electronically signed, no handwritten signature required.",
                    SMALL);
            footer.setSpacingBefore(24);
            footer.setAlignment(Element.ALIGN_CENTER);
            doc.add(footer);

            doc.close();
        } catch (Exception e) {
            throw ApiException.badRequest("Failed to render report PDF / 生成报告 PDF 失败: " + e.getMessage());
        }
        return out.toByteArray();
    }

    private static Paragraph section(String text) {
        Paragraph p = new Paragraph(text, SECTION);
        p.setSpacingBefore(6);
        p.setSpacingAfter(6);
        return p;
    }

    private static void addRow(PdfPTable table, String label, String value) {
        PdfPCell l = new PdfPCell(new Phrase(label, LABEL));
        l.setBackgroundColor(new Color(245, 245, 245));
        l.setPadding(6);
        PdfPCell v = new PdfPCell(new Phrase(value, VALUE));
        v.setPadding(6);
        table.addCell(l);
        table.addCell(v);
    }

    private static void headerCell(PdfPTable table, String text) {
        PdfPCell c = new PdfPCell(new Phrase(text, LABEL));
        c.setBackgroundColor(new Color(238, 242, 250));
        c.setPadding(5);
        table.addCell(c);
    }

    private static void bodyCell(PdfPTable table, String text) {
        PdfPCell c = new PdfPCell(new Phrase(text, VALUE));
        c.setPadding(5);
        table.addCell(c);
    }

    private static String nz(String s) {
        return s == null || s.isBlank() ? "-" : s;
    }
}
