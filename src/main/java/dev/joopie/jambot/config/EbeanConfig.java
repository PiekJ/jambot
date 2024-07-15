package dev.joopie.jambot.config;

import io.ebean.Database;
import io.ebean.DatabaseFactory;
import io.ebean.config.DatabaseConfig;
import io.ebean.datasource.DataSourceConfig;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(SpringDatasourceProperties.class)
public class EbeanConfig {

    @Bean
    public Database ebeanDatabase(SpringDatasourceProperties properties) {
        DataSourceConfig dataSourceConfig = new DataSourceConfig();
        dataSourceConfig.setUrl(properties.getUrl());
        dataSourceConfig.setUsername(properties.getUsername());
        dataSourceConfig.setPassword(properties.getPassword());
        dataSourceConfig.setDriver(properties.getDriverClassName());

        DatabaseConfig config = new DatabaseConfig();
        config.setDataSourceConfig(dataSourceConfig);
        config.setDefaultServer(true);
        config.setDdlGenerate(false);
        config.setDdlRun(false);

        // Specify the packages to search for entities
        config.addPackage("dev.joopie.jambot.models");

        return DatabaseFactory.create(config);
    }
}
