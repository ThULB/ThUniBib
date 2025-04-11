package de.uni_jena.thunibib.his.api.client;

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
import java.util.Map;

class HISInOneClientDefaultImpl implements HISInOneClient {

    private static final Logger LOGGER = LogManager.getLogger(HISInOneClientDefaultImpl.class);
    private final Client jerseyClient;
    protected final String CLIENT_KEY = MCRConfiguration2.getStringOrThrow("ThUniBib.HISinOne.ClientKey");
    protected final String CLIENT_SECRET = MCRConfiguration2.getStringOrThrow("ThUniBib.HISinOne.ClientSecret");

    HISInOneClientDefaultImpl() {
        MCRShutdownHandler.getInstance().addCloseable(this);
        jerseyClient = ClientBuilder.newClient(new ClientConfig().register(HISinOneResolver.class));
    }

    @Override
    public Response get(String path, Map<String, String> parameters, boolean omitToken) {
        WebTarget webTarget = getJerseyClient()
            .target(HISInOneClientDefaultImpl.HIS_IN_ONE_BASE_URL + API_PATH);

        if (parameters != null) {
            for (Map.Entry<String, String> entry : parameters.entrySet()) {
                webTarget = webTarget.queryParam(entry.getKey(), entry.getValue());
            }
        }

        webTarget = webTarget.path(path);

        Token token = null;
        if (!omitToken) {
            try {
                token = fetchToken();
            } catch (Exception e) {
                LOGGER.error("Could not fetch token", e);
                return Response.serverError().entity(e.getMessage()).build();
            }
        }

        Invocation.Builder invocationBuilder = webTarget.request(MediaType.APPLICATION_JSON);
        if (token != null) {
            invocationBuilder.header("Authorization",
                getAuthorizationHeaderValue(AuthType.Bearer, token.getAccessToken()));
        }

        Response response = invocationBuilder.get();
        return response;
    }

    @Override
    public Response post(String path, String bodySource, Map<String, String> parameters) {
        WebTarget webTarget = getJerseyClient()
            .target(HISInOneClientDefaultImpl.HIS_IN_ONE_BASE_URL + API_PATH)
            .path(path);

        if (parameters != null) {
            for (Map.Entry<String, String> entry : parameters.entrySet()) {
                webTarget = webTarget.queryParam(entry.getKey(), entry.getValue());
            }
        }

        Token token;
        try {
            token = fetchToken();
        } catch (Exception e) {
            LOGGER.error("Could not fetch token", e);
            return Response.serverError().entity(e.getMessage()).build();
        }

        Invocation.Builder invocationBuilder = webTarget.request(MediaType.APPLICATION_JSON);
        invocationBuilder.header("Authorization", getAuthorizationHeaderValue(AuthType.Bearer, token.getAccessToken()));

        Entity<String> body = Entity.entity(bodySource, MediaType.APPLICATION_JSON);

        Response response = invocationBuilder.post(body);

        return response;
    }

    @Override
    public Response put(String path, String bodySource) {
        WebTarget webTarget = getJerseyClient()
            .target(HISInOneClientDefaultImpl.HIS_IN_ONE_BASE_URL + API_PATH)
            .path(path);

        Token token;
        try {
            token = fetchToken();
        } catch (Exception e) {
            LOGGER.error("Could not fetch token", e);
            return Response.serverError().entity(e.getMessage()).build();
        }

        Invocation.Builder invocationBuilder = webTarget.request(MediaType.APPLICATION_JSON);
        invocationBuilder.header("Authorization", getAuthorizationHeaderValue(AuthType.Bearer, token.getAccessToken()));

        Entity<String> body = Entity.entity(bodySource, MediaType.APPLICATION_JSON);
        Response response = invocationBuilder.put(body);

        return response;
    }

    protected Token fetchToken() {
        WebTarget webTarget = getJerseyClient()
            .target(HISInOneClientDefaultImpl.HIS_IN_ONE_BASE_URL + API_PATH)
            .path("cs/psv/oauth/token");

        Invocation.Builder invocationBuilder = webTarget.request(MediaType.APPLICATION_JSON);
        invocationBuilder
            .header("Authorization", getAuthorizationHeaderValue(AuthType.Basic, CLIENT_KEY + ":" + CLIENT_SECRET));

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
