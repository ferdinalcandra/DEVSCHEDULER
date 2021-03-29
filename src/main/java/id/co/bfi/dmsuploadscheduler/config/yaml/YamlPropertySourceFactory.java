package id.co.bfi.dmsuploadscheduler.config.yaml;

import java.io.IOException;
import java.util.Properties;

import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.PropertySourceFactory;
import org.springframework.lang.Nullable;

public class YamlPropertySourceFactory implements PropertySourceFactory {

	@Override
    public PropertySource<?> createPropertySource(@Nullable String name, EncodedResource encodedResource) throws IOException {
        PropertySource<?> propertySource = null;
        if (encodedResource != null) {
        	YamlPropertiesFactoryBean factory = new YamlPropertiesFactoryBean();
            factory.setResources(encodedResource.getResource());
            Properties properties = factory.getObject();
            String filename = encodedResource.getResource().getFilename();
            if (filename != null && properties != null)
            	propertySource = new PropertiesPropertySource(filename, properties);
        }
		return propertySource;
    }

}
