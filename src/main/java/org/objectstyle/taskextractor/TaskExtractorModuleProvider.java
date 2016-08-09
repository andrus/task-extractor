package org.objectstyle.taskextractor;

import com.google.inject.Module;
import io.bootique.BQModuleProvider;

public class TaskExtractorModuleProvider implements BQModuleProvider {

    @Override
    public Module module() {
        return new TaskExtractorModule();
    }
}
