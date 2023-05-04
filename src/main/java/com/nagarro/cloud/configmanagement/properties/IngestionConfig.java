package com.nagarro.cloud.configmanagement.properties;

import com.nagarro.cloud.configmanagement.annotations.ManagedConfiguration;
import com.nagarro.cloud.configmanagement.annotations.Property;
import lombok.Getter;
import lombok.ToString;
import org.springframework.stereotype.Component;

@Getter
@ToString
@Component
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
