package com.example.ecommerce.config;

import org.apache.shardingsphere.driver.api.yaml.YamlShardingSphereDataSourceFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

@Configuration
public class DataSourceConfig {

    @Bean
    public DataSource dataSource() throws IOException, SQLException {
        // 加载 classpath:sharding-prod.yml 文件
        File yamlFile = new ClassPathResource("sharding-prod.yml").getFile();
        return YamlShardingSphereDataSourceFactory.createDataSource(yamlFile);
    }
}
