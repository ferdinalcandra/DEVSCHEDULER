package id.co.bfi.dmsuploadscheduler.config.db;

import java.util.Properties;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import id.co.bfi.dmsuploadscheduler.config.yaml.JasyptConfig;
import id.co.bfi.dmsuploadscheduler.config.yaml.TempDataDbConfig;

@Configuration
@EnableJpaRepositories(
  basePackages = "id.co.bfi.dmsuploadscheduler.repository.temp",
        entityManagerFactoryRef = "tempEntityManagerFactory",
        transactionManagerRef = "tempTransactionManager"
)

public class TempDataSourceConfig {
	
	@Autowired
	JasyptConfig jasyptConfig;
	
	@Autowired
	TempDataDbConfig tempDataDbConfig;
	
	@Autowired
	JpaConfig jpaConfig;
	
    @Bean
    public DataSource tempDataSource() {
    	return DataSourceBuilder.create()
            .driverClassName(tempDataDbConfig.getDriverClassName())
            .url(tempDataDbConfig.getUrl())
            .username(tempDataDbConfig.getUsername())
            .password(jasyptConfig.decryptPassword(tempDataDbConfig.getPassword()))
            .build();
    }
 
    @Bean
    public PlatformTransactionManager tempTransactionManager() {
        EntityManagerFactory factory = tempEntityManagerFactory().getObject();
        return new JpaTransactionManager(factory);
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean tempEntityManagerFactory() {
        LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
        factory.setDataSource(tempDataSource());
        factory.setPackagesToScan(new String[]{"id.co.bfi.dmsuploadscheduler.model.temp"});
        factory.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
     
        Properties jpaProperties = new Properties();
        jpaProperties.put("hibernate.show-sql", jpaConfig.getShowSql());
        jpaProperties.put("hibernate.dialect", jpaConfig.getDatabasePlatform());
        factory.setJpaProperties(jpaProperties);
     
        return factory;
    }
 
    @Bean
    public DataSourceInitializer tempDataSourceInitializer() {
        DataSourceInitializer dataSourceInitializer = new DataSourceInitializer();
        dataSourceInitializer.setDataSource(tempDataSource());
        return dataSourceInitializer;
    }   
}
