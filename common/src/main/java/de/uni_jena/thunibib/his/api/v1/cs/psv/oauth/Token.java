package de.uni_jena.thunibib.his.api.v1.cs.psv.oauth;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

/**
 * Path: <code>/api/v1/cs/psv/oauth/token</code>
 * */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Token {

    @JsonProperty("access_token")
    String accessToken;
    @JsonProperty("scope")
    String scope;
    @JsonProperty("token_type")
    String tokenType;
    @JsonProperty("expires_in")
    String expiresIn;

    Instant createdAt;

    public Token() {
        createdAt = Instant.now();
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getScope() {
        return scope;
    }

    public String getExpiresIn() {
        return expiresIn;
    }

    public String getTokenType() {
        return tokenType;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    @Override
    public String toString() {
        return getAccessToken();
    }
}
