
# Configuration Management Using AWS AppConfig

# Overview

This is a demo of how configurations managed in AWS AppConfig can be fetched via Java spring boot based application.

This demo shows how to integrate a Java microservices with AppConfig and fetch configurations in real-time without having to restart the application if the configuration changes.

The solution has the following features -

- Implements an in memory cache to store configurations with a ttl of 2 minutes (configurable) to avoid unnecessary cost of fetching configurations again and again from AppConfig.
- Allows to easily add configurations using `@ManagedConfiguration` and `@Property`.
- These configurations can easily be `@Autowired` in downstream classes.

# Structure

```java
@ManagedConfiguration(configName = "IngestionConfig")
public class IngestionConfig {

  @Property(propertyName = "batchSize")
  private int batchSize;

  @Property(propertyName = "sourceDir")
  private String sourceDir;

  @Property(propertyName = "destDir")
  private String destDir;

  @Property(propertyName = "ingestionEnabled")
  private Boolean ingestionEnabled;

}
```
`@ManagedConfiguration` is a class level annotation which takes `configName` as input param. This config name should be defined in AppConfig.

`@Property` is a field level annotation which takes `propertyName` as input param. These properties should be defined in the JSON file under the configuration.

Sample JSON for above configuration -

```json
{
  "batchSize": 1000,
  "sourceDir": "/features/azure/updated/source-dir",
  "destDir": "/features/azure/updated/dest-dir",
  "ingestionEnabled": true
}
```

# How to Install and Run the Project

This project uses maven as a build tool. Use `mvn clean install` to create a jar file. The app 
requires following environment variables :

- access-key={your-aws-access-key}
- secret-access-key={your-aws-secret-access-key}
- app-name={application name defined in AppConfig}
- env={environment defined in AppConfig on which the configurations are deployed}

After creating .jar file, use below command to run the app.

```
java -Daccess-key=[your-access-key] -Dsecret-access-key=[your-secret-access-key] 
    -Dapp-name=[application name defined in AppConfig] -Denv=[environment defined in AppConfig on which the configurations are deployed] -jar target/config-management-0.0.1-SNAPSHOT.jar 
```


