package org.objectstyle.taskextractor.jaxrs;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.bootique.jackson.JacksonService;
import jakarta.ws.rs.core.Feature;
import jakarta.ws.rs.core.FeatureContext;


public class TaskExtractorFeature implements Feature {

    private JacksonService jacksonService;

    public TaskExtractorFeature(JacksonService jacksonService) {
        this.jacksonService = jacksonService;
    }

    @Override
    public boolean configure(FeatureContext context) {

        ObjectMapper mapper = jacksonService
                .newObjectMapper()
                .registerModule(new JavaTimeModule())
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        context.register(new CommitsMessageBodyReader(mapper))
                .register(new BranchesMessageBodyReader(mapper));

        return true;
    }
}
