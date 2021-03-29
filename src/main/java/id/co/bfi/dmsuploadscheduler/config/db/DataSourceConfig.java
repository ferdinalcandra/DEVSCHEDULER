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
import id.co.bfi.dmsuploadscheduler.config.yaml.JpaConfig;
import id.co.bfi.dmsuploadscheduler.config.yaml.DataDbConfig;

@Configuration
@EnableJpaRepositories(basePackages = "id.co.bfi.dmsuploadscheduler.repository", entityManagerFactoryRef = "entityManagerFactory", transactionManagerRef = "transactionManager")
public class DataSourceConfig {

	private DataDbConfig dataDbConfig;
	private JpaConfig jpaConfig;
	private JasyptConfig jasyptConfig;
	
	@Autowired
	public DataSourceConfig(DataDbConfig dataDbConfig, JpaConfig jpaConfig, JasyptConfig jasyptConfig) {
		this.dataDbConfig = dataDbConfig;
		this.jpaConfig = jpaConfig;
		this.jasyptConfig = jasyptConfig;
	}
	
	@Bean
	public DataSource dataSource() {
		return DataSourceBuilder.create().driverClassName(dataDbConfig.getDriverClassName()).url(dataDbConfig.getUrl())
				.username(dataDbConfig.getUsername()).password(jasyptConfig.decryptPassword(dataDbConfig.getPassword()))
				.build();
	}

	@Bean
	public PlatformTransactionManager transactionManager() {
		EntityManagerFactory factory = entityManagerFactory().getObject();
		return (factory != null) ? new JpaTransactionManager(factory) : null;
	}

	@Bean
	public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
		LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
		
		factory.setDataSource(dataSource());
		factory.setPackagesToScan(new String[] { "id.co.bfi.dmsuploadscheduler.entity" });
		factory.setJpaVendorAdapter(new HibernateJpaVendorAdapter());

		Properties jpaProperties = new Properties();
		jpaProperties.put("hibernate.show-sql", jpaConfig.getShowSql());
		jpaProperties.put("hibernate.dialect", jpaConfig.getDatabasePlatform());
		factory.setJpaProperties(jpaProperties);

		return factory;
	}

	@Bean
	public DataSourceInitializer dataSourceInitializer() {
		DataSourceInitializer dataSourceInitializer = new DataSourceInitializer();
		dataSourceInitializer.setDataSource(dataSource());
		return dataSourceInitializer;
	}

}
