package org.objectstyle.taskextractor.repo;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.bootique.annotation.BQConfig;
import io.bootique.config.PolymorphicConfiguration;
import io.bootique.di.Injector;

@BQConfig("Configurable task extractor.")
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
public interface RepositoryTaskExtractorFactory extends PolymorphicConfiguration {

    RepositoryTaskExtractor createExtractor(Injector injector);
}
