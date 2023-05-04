package com.nagarro.cloud.configmanagement.aspect;

import com.amazonaws.services.appconfig.AmazonAppConfig;
import com.amazonaws.services.appconfig.model.GetConfigurationRequest;
import com.amazonaws.services.appconfig.model.GetConfigurationResult;
import com.amazonaws.services.appconfigdata.AWSAppConfigData;
import com.amazonaws.services.appconfigdata.model.GetLatestConfigurationRequest;
import com.amazonaws.services.appconfigdata.model.GetLatestConfigurationResult;
import com.amazonaws.services.appconfigdata.model.StartConfigurationSessionRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nagarro.cloud.configmanagement.annotations.ManagedConfiguration;
import com.nagarro.cloud.configmanagement.annotations.Property;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class AppConfigAspect {

  private final AmazonAppConfig amazonAppConfig;
  private final AWSAppConfigData appConfigClient;
  private final ObjectMapper objectMapper = new ObjectMapper();
  private final Map<String, String> configTokenMap = new ConcurrentHashMap<>();
  private static final long REFRESH_INTERVAL = 2L;
  private final Map<String, Map<String, String>> configCache = new ConcurrentHashMap<>();

  @Value("${app-name}")
  private String appName;

  @Value("${env}")
  private String env;

  @Autowired
  public AppConfigAspect(AmazonAppConfig amazonAppConfig, AWSAppConfigData appConfigClient) {
    this.amazonAppConfig = amazonAppConfig;
    this.appConfigClient = appConfigClient;
    ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    scheduler.scheduleAtFixedRate(this::refreshCache, 0L, REFRESH_INTERVAL, TimeUnit.MINUTES);
  }

  @Before("@within(com.nagarro.cloud.configmanagement.annotations.ManagedConfiguration)")
  public void populateConfigProperties(JoinPoint joinPoint)
      throws IllegalAccessException {
    Object target = joinPoint.getTarget();
    Class<?> targetClass = target.getClass();

    String configName = targetClass.getAnnotation(ManagedConfiguration.class).configName();

    Map<String, String> properties = configCache.computeIfAbsent(configName,
        k -> {
          log.info("Managed Configuration Cache Miss for [{}]", configName);
          return getAppConfigs(configName);
        });

    for (Field field : targetClass.getDeclaredFields()) {
      if (field.isAnnotationPresent(Property.class)) {
        String propertyName = field.getAnnotation(Property.class).propertyName();
        if (properties.containsKey(propertyName)) {
          field.setAccessible(true);
          field.set(target, properties.get(propertyName));
        }
      }
    }

  }

  /*
   * The older way of getting app configurations. Now deprecated.
   * */
  @SneakyThrows
  private Map<String, String> getApplicationConfigurations(String configName) {

    GetConfigurationRequest request = new GetConfigurationRequest()
        .withApplication(appName)
        .withEnvironment(env)
        .withClientId(UUID.randomUUID().toString())
        .withConfiguration(configName);

    GetConfigurationResult result = amazonAppConfig.getConfiguration(request);

    return objectMapper.readValue(
        new String(result.getContent().array(), Charset.defaultCharset()), Map.class);

  }

  @SneakyThrows
  public Map<String, String> getAppConfigs(String configName) {
    log.info("Getting Managed Configurations for [{}]", configName);
    /*
     * The StartConfigurationSession API should only be called once per application, environment,
     * configuration profile, and client to establish a session with the service.
     * This is typically done in the startup of your application or immediately prior to the
     * first retrieval of a configuration
     * */
    StartConfigurationSessionRequest sessionRequest = new StartConfigurationSessionRequest()
        .withApplicationIdentifier("order-management-system")
        .withEnvironmentIdentifier("dev")
        .withConfigurationProfileIdentifier(configName);

    /*
     * initialConfigurationToken should be only generated once per app,env and config level.
     * */
    configTokenMap.computeIfAbsent(configName,
        k -> appConfigClient.startConfigurationSession(sessionRequest)
            .getInitialConfigurationToken());

    GetLatestConfigurationRequest latestConfigReq = new GetLatestConfigurationRequest()
        .withConfigurationToken(configTokenMap.get(configName));

    GetLatestConfigurationResult latestResult = appConfigClient.getLatestConfiguration(
        latestConfigReq);

    configTokenMap.put(configName, latestResult.getNextPollConfigurationToken());

    String properties = StandardCharsets.UTF_8.decode(latestResult.getConfiguration()).toString();

    return StringUtils.isBlank(properties) ? Collections.emptyMap()
        : objectMapper.readValue(properties, Map.class);

  }

  private void refreshCache() {
    configCache.clear();
    log.info("Managed Configurations Cache evicted");
  }
}
