package org.objectstyle.taskextractor;


import com.google.inject.Binder;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import io.bootique.BQCoreModule;
import io.bootique.ConfigModule;
import io.bootique.config.ConfigurationFactory;
import io.bootique.jackson.JacksonService;
import io.bootique.jersey.client.HttpClientFactory;
import io.bootique.jersey.client.JerseyClientModule;

public class TaskExtractorModule extends ConfigModule {

    @Override
    public void configure(Binder binder) {
        BQCoreModule.setDefaultCommand(binder, ExtractCommand.class);
        JerseyClientModule.contributeFeatures(binder).addBinding().to(TaskExtractorFeature.class);
        BQCoreModule.setApplicationDescription(binder, "Extracts programming activities from various sources.");
    }

    @Singleton
    @Provides
    private TaskExtractor provideExtractor(HttpClientFactory clientFactory, ConfigurationFactory configurationFactory) {
        return configurationFactory.config(TaskExtractorFactory.class, configPrefix).createExtractor(clientFactory);
    }

    @Singleton
    @Provides
    private TaskExtractorFeature provideExtractorFeature(JacksonService jacksonService) {
        return new TaskExtractorFeature(jacksonService);
    }
}
