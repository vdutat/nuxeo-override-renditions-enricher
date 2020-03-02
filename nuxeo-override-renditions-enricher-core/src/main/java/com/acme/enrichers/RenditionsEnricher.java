package com.acme.enrichers;

import static org.nuxeo.ecm.core.io.registry.reflect.Instantiations.SINGLETON;
import static org.nuxeo.ecm.core.io.registry.reflect.Priorities.REFERENCE;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.io.registry.reflect.Setup;
import org.nuxeo.ecm.platform.picture.api.ImagingDocumentConstants;
import org.nuxeo.ecm.platform.rendition.Rendition;
import org.nuxeo.ecm.platform.rendition.io.RenditionJsonEnricher;
import org.nuxeo.ecm.platform.rendition.service.RenditionService;
import org.nuxeo.runtime.api.Framework;

import com.fasterxml.jackson.core.JsonGenerator;

/**
 * Enrich {@link nuxeo.ecm.core.api.DocumentModel} Json.
 * <p>
 * Format is:
 * </p>
 * <pre>
 * {@code
 * {
 *   ...
 *   "contextParameters": {
*     "renditions": { ... }
 *   }
 * }}
 * </pre>
 */
@Setup(mode = SINGLETON, priority = REFERENCE)
public class RenditionsEnricher extends RenditionJsonEnricher {

    public RenditionsEnricher() {
        super();
    }

    @Override
    public void write(JsonGenerator jg, DocumentModel document) throws IOException {
        List<Rendition> renditions = getRenditions(document);
        jg.writeArrayFieldStart(NAME);
        for (Rendition rendition : renditions) {
            jg.writeStartObject();
            jg.writeStringField("name", rendition.getName());
            jg.writeStringField("kind", rendition.getKind());
            jg.writeStringField("icon", ctx.getBaseUrl().replaceAll("/$", "") + rendition.getIcon());
            jg.writeStringField("url", String.format(RENDITION_REST_URL_FORMAT, ctx.getBaseUrl(), document.getId(),
                rendition.getName()));
            jg.writeEndObject();
        }
        jg.writeEndArray();
    }

    protected List<Rendition> getRenditions(DocumentModel document) {
        RenditionService renditionService = Framework.getService(RenditionService.class);
        List<Rendition> renditions = renditionService.getAvailableRenditions(document, true);
        return renditions.stream()
          .filter(rendition -> !document.getFacets().contains(ImagingDocumentConstants.PICTURE_FACET) || (document.getFacets().contains(ImagingDocumentConstants.PICTURE_FACET) && !"cmis:thumbnail".equals(rendition.getKind())) )
          .collect(Collectors.toList());
    }
}
