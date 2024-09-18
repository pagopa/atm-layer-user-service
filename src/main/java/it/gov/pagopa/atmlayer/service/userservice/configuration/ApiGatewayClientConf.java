package it.gov.pagopa.atmlayer.service.userservice.configuration;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.Getter;
import software.amazon.awssdk.auth.credentials.WebIdentityTokenFileCredentialsProvider;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.apigateway.ApiGatewayClient;

@ApplicationScoped
public class ApiGatewayClientConf {

    @Getter
    private ApiGatewayClient apiGatewayClient;

    @PostConstruct
    void init() {
        this.apiGatewayClient = ApiGatewayClient.builder()
                .httpClient(ApacheHttpClient.builder().build())
                .region(Region.EU_SOUTH_1)
                .credentialsProvider(WebIdentityTokenFileCredentialsProvider.create())
                .build();
    }
}
