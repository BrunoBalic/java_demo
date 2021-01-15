package com.bb.WaterFlowMeter;

// klasa u kojoj drzim credentials za dohvat tokena
// ovo koristim kada koristim obicnu java aplikaciju
// kada bih radio Spring aplicakiju onda ove stvari zapisem u application.properties
public class SecurityCredentials {
    private String client_id;
    private String client_secret;
    private String username;
    private String password;
    private String token_url;

    public SecurityCredentials(String client_id, String client_secret, String username, String password, String token_url) {
        this.client_id = client_id;
        this.client_secret = client_secret;
        this.username = username;
        this.password = password;
        this.token_url = token_url;
    }

    public String getClient_id() {
        return client_id;
    }
    public String getClient_secret() {
        return client_secret;
    }
    public String getUsername() {
        return username;
    }
    public String getPassword() {
        return password;
    }
    public String getToken_url() {
        return token_url;
    }
}
