package org.objectstyle.taskextractor;


import com.google.inject.Binder;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import io.bootique.BQCoreModule;
import io.bootique.Bootique;
import io.bootique.ConfigModule;
import io.bootique.config.ConfigurationFactory;
import io.bootique.jackson.JacksonService;
import io.bootique.jersey.client.JerseyClientModule;

public class TaskExtractorModule extends ConfigModule {

    public static void main(String[] args) {
        Bootique.app("--config=andrus.yml").args(args).autoLoadModules().run();
    }

    @Override
    public void configure(Binder binder) {
        BQCoreModule.extend(binder)
                .setDefaultCommand(ExtractCommand.class)
                .setApplicationDescription("Extracts programming activities from various sources.")
                .declareVar("jerseyclient.auth.github.password", "TE_GITHUB_PASSWORD");
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
