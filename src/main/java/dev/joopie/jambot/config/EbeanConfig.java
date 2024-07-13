package dev.joopie.jambot.config;

import io.ebean.config.DatabaseConfig;
import io.ebean.datasource.DataSourceConfig;
import io.ebean.Database;
import io.ebean.DatabaseFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EbeanConfig {

    @Bean
    public Database ebeanDatabase() {
        DataSourceConfig dataSourceConfig = new DataSourceConfig();
        dataSourceConfig.setUrl("jdbc:mariadb://localhost:3306/jambot");
        dataSourceConfig.setUsername("root");
        dataSourceConfig.setPassword("root");
        dataSourceConfig.setDriver("org.mariadb.jdbc.Driver");

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
