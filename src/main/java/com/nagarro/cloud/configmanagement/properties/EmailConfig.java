package com.nagarro.cloud.configmanagement.properties;

import com.nagarro.cloud.configmanagement.annotations.ManagedConfiguration;
import com.nagarro.cloud.configmanagement.annotations.Property;
import lombok.Getter;
import lombok.ToString;
import org.springframework.stereotype.Component;

@Getter
@ToString
@Component
@ManagedConfiguration(configName = "EmailConfig")
public class EmailConfig {

  @Property(propertyName = "smtpHost")
  private String smtpHost;

  @Property(propertyName = "smtpPort")
  private Integer smtpPort;

}
