package com.bb.WaterFlowMeter.WebClient;

import com.bb.WaterFlowMeter.Models.KeycloakAccessToken;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

// trebao bi imati samo jednu instancu ove klase u aplikaciji (klase koja ima WebClient)
public class WebClientImplementation {

    private WebClient client;

    public WebClientImplementation() {
        // prilikom kreiranja necu postaviti base URL jer ga poslje ne mogu mjenjat ja mislim ??
        // pa cu onda kao uri slati puni url path
        // fullPath = url + uri , mislim da je ovo logika
        this.client = WebClient.create();
    }

    // master metoda
    public <T, G> Map.Entry<HttpStatus, G> makePOSTRequest(
            String fullURL, MultiValueMap<String, String> headers, T bodyObject, Class<G> returnClass) {

        ClientResponse clientResponse = this.client.post()
                .uri(fullURL)
                .headers(httpHeaders -> httpHeaders.addAll(headers))
                .bodyValue(bodyObject)
                .exchange()
                .onErrorResume(e -> Mono.empty())
                .block();

        if (clientResponse == null) {
            System.out.println("client respones je NULL");
            return null;
        }

        HttpStatus httpStatus = clientResponse.statusCode();

        if (httpStatus.is2xxSuccessful()) {
            G g = clientResponse.bodyToMono(returnClass).block();
            // vracam HttpStatus i G
            // odlucio sam se za ovaj nacin iako nije bas optimiziran jer imam samo jedan par
            // u mapi pa moram komplicirati da dodem do prvog...
            // drugi nacin bi bio napraviti wrapper klasu u kojoj je HttpStatus i G
            // pa nju vratiti ali to mi nije bas lijepo rijesenje...
            Map<HttpStatus, G> map = new HashMap<>();
            map.put(httpStatus, g);
            return map.entrySet().iterator().next();
        } else {
            Map<HttpStatus, G> map = new HashMap<>();
            map.put(httpStatus, null);
            return map.entrySet().iterator().next();
        }
    }

    public KeycloakAccessToken getNewAccessToken(
            String tokenURL, String client_id, String client_secret,
            String username, String password) {

        ClientResponse clientResponse = this.client.post()
                .uri(tokenURL)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("grant_type", "password")
                        .with("client_id", client_id)
                        .with("client_secret", client_secret)
                        .with("username", username)
                        .with("password", password))
                .exchange()
                .onErrorResume(e -> Mono.empty())
                .block();

        if (clientResponse == null) return null;

        HttpStatus status = clientResponse.statusCode();

        if (status.is2xxSuccessful()) {
            return clientResponse.bodyToMono(KeycloakAccessToken.class).block();
        } else if (status.value() == 401) { // HTTP 401 Unauthorized
            System.out.println("moj error - Error in my method, getting new access token: HTTP 401 Unauthorized");
            return null;
        } else {
            System.out.println("moj error - Error in my method, getting new access token");
            return null;
        }
    }

    // ova metoda nije nuzno potrebana, ne mora se koristiti refresh token ali eto za demonstraciju...
    public KeycloakAccessToken getNewAccessTokenByRefreshToken(
            String tokenURL, String client_id, String client_secret, String refresh_token) {

        ClientResponse clientResponse = this.client.post()
                .uri(tokenURL)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("grant_type", "refresh_token")
                        .with("client_id", client_id)
                        .with("client_secret", client_secret)
                        .with("refresh_token", refresh_token))
                .exchange()
                .onErrorResume(e -> Mono.empty())
                .block();

        if (clientResponse == null) return null;

        HttpStatus status = clientResponse.statusCode();

        if (status.is2xxSuccessful()) {
            return clientResponse.bodyToMono(KeycloakAccessToken.class).block();
        } else {
            System.out.println("Neki error u dobavljanju access tokena preko refresh tokena.");
            return null;
        }
    }

}
