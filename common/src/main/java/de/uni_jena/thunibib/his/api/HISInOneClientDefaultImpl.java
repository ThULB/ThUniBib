package de.uni_jena.thunibib.his.api;

import de.uni_jena.thunibib.his.api.v1.cs.psv.oauth.Token;
import de.uni_jena.thunibib.his.xml.HISinOneResolver;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.jersey.client.ClientConfig;
import org.mycore.common.config.MCRConfiguration2;
import org.mycore.common.events.MCRShutdownHandler;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

class HISInOneClientDefaultImpl implements HISInOneClient {

    private static final Logger LOGGER = LogManager.getLogger(HISInOneClientDefaultImpl.class);
    private static HISInOneClientDefaultImpl instance;
    private final String clientKey;
    private final String clientSecret;
    private final Client jerseyClient;

    HISInOneClientDefaultImpl() {
        clientKey = MCRConfiguration2.getStringOrThrow("ThUniBib.HISinOne.ClientKey");
        clientSecret = MCRConfiguration2.getStringOrThrow("ThUniBib.HISinOne.ClientSecret");
        jerseyClient = ClientBuilder.newClient(new ClientConfig().register(HISinOneResolver.class));
        MCRShutdownHandler.getInstance().addCloseable(this);
    }

    public Response get(String path) {
        WebTarget webTarget = getJerseyClient()
            .target(HISInOneClientDefaultImpl.HIS_IN_ONE_BASE_URL + API_PATH)
            .path(path);

        Token token = fetchToken();
        Invocation.Builder invocationBuilder = webTarget.request(MediaType.APPLICATION_JSON);
        invocationBuilder.header("Authorization", getAuthorizationHeaderValue(AuthType.Bearer, token.getAccessToken()));

        Response response = invocationBuilder.get();
        return response;
    }

    protected Token fetchToken() {
        WebTarget webTarget = getJerseyClient()
            .target(HISInOneClientDefaultImpl.HIS_IN_ONE_BASE_URL + API_PATH)
            .path("cs/psv/oauth/token");

        Invocation.Builder invocationBuilder = webTarget.request(MediaType.APPLICATION_JSON);
        invocationBuilder
            .header("Authorization", getAuthorizationHeaderValue(AuthType.Basic, clientKey + ":" + clientSecret));

        Entity<String> body = Entity.entity("grant_type=client_credentials", MediaType.APPLICATION_FORM_URLENCODED);
        try (Response response = invocationBuilder.post(body)) {
            return response.readEntity(Token.class);
        }
    }

    private String getAuthorizationHeaderValue(AuthType authType, String in) {
        return switch (authType) {
            case Bearer -> authType.name() + " " + in;
            case Basic ->
                authType.name() + " " + Base64.getEncoder().encodeToString(in.getBytes(StandardCharsets.UTF_8));
        };
    }

    protected Client getJerseyClient() {
        return jerseyClient;
    }

    @Override
    public void close() {
        getJerseyClient().close();
    }
}