package org.objectstyle.taskextractor;

import com.google.inject.Module;
import io.bootique.BQModule;
import io.bootique.BQModuleProvider;

public class TaskExtractorModuleProvider implements BQModuleProvider {

    @Override
    public Module module() {
        return new TaskExtractorModule();
    }

    @Override
    public BQModule.Builder moduleBuilder() {
        return BQModuleProvider.super
                .moduleBuilder()
                .description("Provides command to extract coding activity from various repositories. " +
                        "Assists in tracking and such.");
    }
}
