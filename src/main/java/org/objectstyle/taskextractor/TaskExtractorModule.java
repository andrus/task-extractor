package org.objectstyle.taskextractor;

import io.bootique.BQCoreModule;
import io.bootique.Bootique;
import io.bootique.ConfigModule;
import io.bootique.config.ConfigurationFactory;
import io.bootique.di.Binder;
import io.bootique.di.Injector;
import io.bootique.di.Provides;
import io.bootique.jackson.JacksonService;
import io.bootique.jersey.client.JerseyClientModule;
import org.objectstyle.taskextractor.jaxrs.TaskExtractorFeature;

import javax.inject.Singleton;

public class TaskExtractorModule extends ConfigModule {

    public static void main(String[] args) {
        Bootique.app(args).autoLoadModules().exec().exit();
    }

    @Override
    public void configure(Binder binder) {
        BQCoreModule.extend(binder)
                .setDefaultCommand(ExtractCommand.class)
                .setApplicationDescription("Extracts programming activities from various sources.")
                .declareVar("jerseyclient.auth.github.password", "TE_GITHUB_TOKEN", "Github personal token");
        JerseyClientModule.extend(binder).addFeature(TaskExtractorFeature.class);
    }

    @Singleton
    @Provides
    private TaskExtractor provideExtractor(ConfigurationFactory configurationFactory, Injector injector) {
        return configurationFactory.config(TaskExtractorFactory.class, configPrefix).createExtractor(injector);
    }

    @Singleton
    @Provides
    private TaskExtractorFeature provideExtractorFeature(JacksonService jacksonService) {
        return new TaskExtractorFeature(jacksonService);
    }
}
