package com.nagarro.cloud.configmanagement.controller;

import com.google.common.base.Stopwatch;
import com.nagarro.cloud.configmanagement.properties.EmailConfig;
import com.nagarro.cloud.configmanagement.properties.IngestionConfig;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/*
 * This controller is only for testing purpose. Consider it as a service which depends on one or
 * more configurations that are managed in cloud.
 * */
@Slf4j
@RestController
public class ConfigManagementController {

  private final EmailConfig emailConfig;
  private final IngestionConfig ingestionConfig;

  @Autowired
  public ConfigManagementController(EmailConfig emailConfig, IngestionConfig ingestionConfig) {
    this.emailConfig = emailConfig;
    this.ingestionConfig = ingestionConfig;
  }

  @GetMapping("/")
  public String getAllConfigs() {
    Stopwatch stopwatch = Stopwatch.createStarted();
    log.info("Email Config [{}]", emailConfig);
    log.info("Ingestion Config [{}]", ingestionConfig);
    log.info("Time Taken [{}] seconds", stopwatch.elapsed(TimeUnit.SECONDS));
    return emailConfig.toString() + " ; " + ingestionConfig.toString();

  }
}
