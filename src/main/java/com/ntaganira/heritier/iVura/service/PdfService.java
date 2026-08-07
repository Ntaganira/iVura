package com.ntaganira.heritier.iVura.service;

import com.openhtmltopdf.outputdevice.helper.BaseRendererBuilder;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

/**
 * <pre>
 * - Project   : iVura - Hospital Management System
 * - Package   : com.ntaganira.heritier.iVura.service
 * - File      : PdfService.java
 * - Desc      : Renders a Thymeleaf template to HTML and converts it to PDF
 *               using OpenHTMLToPDF (pure Java, no external binary).
 * </pre>
 */
@Service
public class PdfService {

    private final TemplateEngine templateEngine;

    public PdfService(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    public byte[] renderPdf(String template, Map<String, Object> model) {
        Context context = new Context();
        context.setLocale(LocaleContextHolder.getLocale());
        if (model != null) {
            context.setVariables(model);
        }
        String html = templateEngine.process(template, context);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (OutputStream os = baos) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.withHtmlContent(html, null);
            builder.useDefaultPageSize(210f, 297f, BaseRendererBuilder.PageSizeUnits.MM);
            builder.toStream(os);
            builder.run();
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate PDF", e);
        }
        return baos.toByteArray();
    }
}
