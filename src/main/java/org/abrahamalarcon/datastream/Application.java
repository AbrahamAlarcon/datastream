package org.abrahamalarcon.datastream;

import org.abrahamalarcon.datastream.aspect.audit.AuditLoggingAspect;
import org.abrahamalarcon.datastream.dao.WeatherDAO;
import org.abrahamalarcon.datastream.service.audit.AuditEventFactory;
import org.abrahamalarcon.datastream.service.audit.AuditEventLogger;
import org.abrahamalarcon.datastream.service.audit.datasource.DatasourceAuditEventLogger;
import org.abrahamalarcon.datastream.service.audit.datasource.DatasourceAuditLoggingService;
import org.abrahamalarcon.datastream.util.BaseInputValidator;
import org.abrahamalarcon.datastream.util.ExceptionHandlerAspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.system.ApplicationPidFileWriter;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.cache.ehcache.EhCacheManagerFactoryBean;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.Executor;

//import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

/**
 * Created by AbrahamAlarcon on 12/26/2016.
 */
@SpringBootApplication
@ComponentScan
@EnableAsync
@EnableScheduling
@PropertySources({
        @PropertySource("classpath:environment.properties.${springprofilesactive}")
})
@ConfigurationProperties(prefix = "server", ignoreUnknownFields = true)
@EnableCircuitBreaker
@EnableCaching
public class Application
{
    @Autowired private Environment env;

    @Bean
    public AuditEventFactory auditEventFactory() {
        return new AuditEventFactory();
    }

    @Bean
    public DatasourceAuditLoggingService datasourceAuditLoggingService() {
        return new DatasourceAuditLoggingService();
    }

    @Bean
    public AuditEventLogger auditEventLogger() {
        return new AuditEventLogger();
    }

    @Bean
    public DatasourceAuditEventLogger datasourceAuditEventLogger() {
        return new DatasourceAuditEventLogger();
    }

    @Bean
    public AuditLoggingAspect auditLoggingAspect() {
        return new AuditLoggingAspect();
    }

    @Bean(name = "loggingThreadPoolTaskExecutor")
    public Executor loggingThreadPoolTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(Integer.parseInt(env.getProperty("logging.threadPoolTaskExecutor.corePoolSize")));
        executor.setMaxPoolSize(Integer.parseInt(env.getProperty("logging.threadPoolTaskExecutor.maxPoolSize")));
        executor.setQueueCapacity(Integer.parseInt(env.getProperty("logging.threadPoolTaskExecutor.queueCapacity")));
        executor.setThreadNamePrefix(env.getProperty("logging.threadPoolTaskExecutor.threadNamePrefix"));
        executor.initialize();
        return executor;
    }

    @Bean
    public MessageSource messageSource() {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename("messages");
        return messageSource;
    }

    @Bean
    public MessageSource errorCodeMessageSource() {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename("error_codes");
        return messageSource;
    }

    @Bean
    public ExceptionHandlerAspect exceptionHandlerAspect()
    {
        return new ExceptionHandlerAspect();
    }

    @Bean
    public BaseInputValidator inputValidator() throws Exception {
        InputValidators inputValidators = new InputValidators();
        BaseInputValidator inputValidator = new BaseInputValidator();
        inputValidator.setValidators(inputValidators.validators());
        return inputValidator;
    }

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }
    
    @Bean
    public RetryTemplate retryRestTemplate()
    {
        SimpleRetryPolicy simpleRetryPolicy = new SimpleRetryPolicy();
        simpleRetryPolicy.setMaxAttempts(Integer.parseInt(env.getProperty("weather.retry.policy.maxAttempts")));
        FixedBackOffPolicy fixedBackOffPolicy = new FixedBackOffPolicy();
        fixedBackOffPolicy.setBackOffPeriod(Long.parseLong(env.getProperty("weather.retry.policy.backOffPeriodMillis")));
        RetryTemplate retryTemplate = new RetryTemplate();
        retryTemplate.setRetryPolicy(simpleRetryPolicy);
        retryTemplate.setBackOffPolicy(fixedBackOffPolicy);
        return retryTemplate;
    }

    @Bean
    public WeatherDAO weatherDAO()
    {
        WeatherDAO weatherDAO = new WeatherDAO();
        weatherDAO.setUrl(env.getProperty("weather.url"));
        return weatherDAO;
    }

    @Bean
    public CacheManager getEhCacheManager(){
        return  new EhCacheCacheManager(getEhCacheFactory().getObject());
    }

    @Bean
    public EhCacheManagerFactoryBean getEhCacheFactory(){
        EhCacheManagerFactoryBean factoryBean = new EhCacheManagerFactoryBean();
        factoryBean.setConfigLocation(new ClassPathResource("ehcache.xml"));
        factoryBean.setShared(true);
        return factoryBean;
    }

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(Application.class);
        application.addListeners(new ApplicationPidFileWriter("app.pid"));
        application.run(args);
    }

}
