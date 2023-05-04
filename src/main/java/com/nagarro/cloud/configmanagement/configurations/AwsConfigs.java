package com.nagarro.cloud.configmanagement.configurations;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.appconfig.AmazonAppConfig;
import com.amazonaws.services.appconfig.AmazonAppConfigClientBuilder;
import com.amazonaws.services.appconfigdata.AWSAppConfigData;
import com.amazonaws.services.appconfigdata.AWSAppConfigDataClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AwsConfigs {

  @Value("${access-key}")
  private String accessKey;

  @Value("${secret-access-key}")
  private String secretAccessKey;

  @Bean
  public AWSCredentialsProvider getBasicAwsCredentials() {
    return new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKey, secretAccessKey));
  }

  @Bean
  public AmazonAppConfig getAmazonAppConfig(AWSCredentialsProvider awsCredentials) {
    return AmazonAppConfigClientBuilder.standard()
        .withCredentials(awsCredentials)
        .withRegion(Regions.US_EAST_1)
        .build();
  }

  @Bean
  public AWSAppConfigData getAwsAppClient(AWSCredentialsProvider awsCredentials) {
    return AWSAppConfigDataClient.builder()
        .withCredentials(awsCredentials)
        .withRegion(Regions.US_EAST_1)
        .build();
  }

}
