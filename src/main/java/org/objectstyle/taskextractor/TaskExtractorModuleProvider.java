package org.objectstyle.taskextractor;


import io.bootique.BQModuleMetadata;
import io.bootique.BQModuleProvider;
import io.bootique.di.BQModule;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Map;

public class TaskExtractorModuleProvider implements BQModuleProvider {

    @Override
    public BQModule module() {
        return new TaskExtractorModule();
    }

    @Override
    public BQModuleMetadata.Builder moduleBuilder() {
        return BQModuleProvider.super
                .moduleBuilder()
                .description("Provides command to extract coding activity from various repositories. " +
                        "Assists in tracking and such.");
    }

    @Override
    public Map<String, Type> configs() {
        return Collections.singletonMap("taskextractor", TaskExtractorFactory.class);
    }
}
