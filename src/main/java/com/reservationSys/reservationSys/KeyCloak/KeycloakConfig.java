package com.reservationSys.reservationSys.KeyCloak;


import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KeycloakConfig {

    @Value("${KEYCLOAK_CLIENT_SECRET}")
    private String clientSecret;

    @Bean
    public Keycloak keycloak(){
        return KeycloakBuilder.builder()
                .serverUrl("http://localhost8081")
                .realm("voltbook")
                .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
                .clientId("spring-boot-client")
                .clientSecret(clientSecret)
                .build();
    }

}
