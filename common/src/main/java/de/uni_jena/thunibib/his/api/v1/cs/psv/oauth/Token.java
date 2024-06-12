package de.uni_jena.thunibib.his.api.v1.cs.psv.oauth;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Token {

    @JsonProperty("access_token")
    String accessToken;
    @JsonProperty("scope")
    String scope;
    @JsonProperty("token_type")
    String tokenType;
    @JsonProperty("expires_in")
    String expiresIn;

    public Token() {
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(String expiresIn) {
        this.expiresIn = expiresIn;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    @Override
    public String toString() {
        return getAccessToken();
    }
}
