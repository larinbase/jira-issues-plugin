package com.atlassian.tutorial.myPlugin.config;

import com.atlassian.tutorial.myPlugin.api.MyPluginComponent;
import com.atlassian.tutorial.myPlugin.impl.MyPluginComponentImpl;
import com.atlassian.plugins.osgi.javaconfig.configs.beans.ModuleFactoryBean;
import com.atlassian.plugins.osgi.javaconfig.configs.beans.PluginAccessorBean;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.tutorial.myPlugin.telegram.TelegramPollingThread;
import org.osgi.framework.ServiceRegistration;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import static com.atlassian.plugins.osgi.javaconfig.OsgiServices.exportOsgiService;
import static com.atlassian.plugins.osgi.javaconfig.OsgiServices.importOsgiService;

@Configuration
@Import({
        ModuleFactoryBean.class,
        PluginAccessorBean.class
})
public class MyPluginJavaConfig {

    // imports ApplicationProperties from OSGi
    @Bean
    public ApplicationProperties applicationProperties() {
        return importOsgiService(ApplicationProperties.class);
    }

    @Bean
    public MyPluginComponent myPluginComponent(ApplicationProperties applicationProperties) {
        return new MyPluginComponentImpl(applicationProperties);
    }

    // Exports MyPluginComponent as an OSGi service
    @Bean
    public FactoryBean<ServiceRegistration> registerMyDelegatingService(
            final MyPluginComponent mypluginComponent) {
        return exportOsgiService(mypluginComponent, null, MyPluginComponent.class);
    }

    @Bean(initMethod = "start", destroyMethod = "interrupt")
    public TelegramPollingThread telegramPollingThread() {
        String token = System.getenv("TELEGRAM_BOT_TOKEN");
        if (token == null || token.isEmpty()) {
            throw new IllegalStateException("TELEGRAM_BOT_TOKEN is not set in environment");
        }
        return new TelegramPollingThread(token);
    }

}