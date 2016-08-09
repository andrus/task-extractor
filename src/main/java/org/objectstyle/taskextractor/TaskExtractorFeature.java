package org.objectstyle.taskextractor;

import io.bootique.jackson.JacksonService;

import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;

public class TaskExtractorFeature implements Feature {

    private JacksonService jacksonService;

    public TaskExtractorFeature(JacksonService jacksonService) {
        this.jacksonService = jacksonService;
    }

    @Override
    public boolean configure(FeatureContext context) {
        context.register(new CommitsMessageBodyReader(jacksonService));
        return true;
    }
}
