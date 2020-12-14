package id.co.bfi.dmsuploadscheduler.config.db;

import java.util.Properties;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import id.co.bfi.dmsuploadscheduler.config.yaml.JasyptConfig;
import id.co.bfi.dmsuploadscheduler.config.yaml.MsDataDbConfig;

@Configuration
@EnableJpaRepositories(
    basePackages = "id.co.bfi.dmsuploadscheduler.repository.ms",
    entityManagerFactoryRef = "msEntityManagerFactory",
    transactionManagerRef = "msTransactionManager"
)

public class MsDataSourceConfig {
	
	@Autowired
	JasyptConfig jasyptConfig;
	
	@Autowired
	MsDataDbConfig msDataDbConfig;
	
	@Autowired
	JpaConfig jpaConfig;
	
    @Bean
    @Primary
    public DataSource msDataSource() {
        return DataSourceBuilder.create()
	        .driverClassName(msDataDbConfig.getDriverClassName())
	        .url(msDataDbConfig.getUrl())
	        .username(msDataDbConfig.getUsername())
	        .password(jasyptConfig.decryptPassword(msDataDbConfig.getPassword()))
	        .build();
    }

    @Bean
    @Primary
    public PlatformTransactionManager msTransactionManager() {
        EntityManagerFactory factory = msEntityManagerFactory().getObject();
        return new JpaTransactionManager(factory);
    }

    @Bean
    @Primary
    public LocalContainerEntityManagerFactoryBean msEntityManagerFactory() {
        LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
        factory.setDataSource(msDataSource());
        factory.setPackagesToScan(new String[] {
            "id.co.bfi.dmsuploadscheduler.model.ms"
        });
        factory.setJpaVendorAdapter(new HibernateJpaVendorAdapter());

        Properties jpaProperties = new Properties();
        jpaProperties.put("hibernate.show-sql", jpaConfig.getShowSql());
        jpaProperties.put("hibernate.dialect", jpaConfig.getDatabasePlatform());
        factory.setJpaProperties(jpaProperties);

        return factory;

    }

    @Bean
    @Primary
    public DataSourceInitializer msDataSourceInitializer() {
        DataSourceInitializer dataSourceInitializer = new DataSourceInitializer();
        dataSourceInitializer.setDataSource(msDataSource());
        return dataSourceInitializer;
    }
}
