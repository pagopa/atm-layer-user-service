package it.gov.pagopa.atmlayer.service.userservice.configuration;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.Getter;
import software.amazon.awssdk.auth.credentials.WebIdentityTokenFileCredentialsProvider;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;

@ApplicationScoped
public class CognitoClientConf {

    @Getter
    private CognitoIdentityProviderClient cognitoClient;

    @PostConstruct
    void init() {
        this.cognitoClient = CognitoIdentityProviderClient.builder()
                .httpClientBuilder(ApacheHttpClient.builder())
                .region(Region.EU_SOUTH_1)
                .credentialsProvider(WebIdentityTokenFileCredentialsProvider.create())
                .build();
    }

}
