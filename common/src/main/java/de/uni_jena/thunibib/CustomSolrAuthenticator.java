package de.uni_jena.thunibib;

import org.apache.solr.client.solrj.SolrRequest;
import org.mycore.solr.auth.MCRSolrAuthenticator;

import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Implementation of {@link MCRSolrAuthenticator} that applies HTTP Basic
 * Authentication credentials to Solr requests.
 * <p>
 * This class supports authentication for both SolrJ {@link SolrRequest}
 * instances and Java {@link HttpRequest.Builder} instances. Credentials are
 * provided during construction and are applied unchanged to every request.
 * </p>
 *
 * @author shermann (Silvio Hermann)
 */
public class CustomSolrAuthenticator implements MCRSolrAuthenticator {

    /** Username used for HTTP Basic Authentication. */
    private String username;

    /** Password used for HTTP Basic Authentication. */
    private String password;

    public CustomSolrAuthenticator(String username, String password) {
        this.username = username;
        this.password = password;
    }

    /**
     * Applies HTTP Basic Authentication credentials to a SolrJ request.
     *
     * @param request the Solr request to authenticate
     */
    @Override
    public void applyAuthentication(SolrRequest<?> request) {
        request.setBasicAuthCredentials(this.username, this.password);
    }

    /**
     * Applies HTTP Basic Authentication credentials to a Java HTTP request.
     * <p>
     * The credentials are encoded according to the HTTP Basic Authentication
     * specification and added as an {@code Authorization} header.
     * </p>
     *
     * @param request the HTTP request builder to authenticate
     */
    @Override
    public void applyAuthentication(HttpRequest.Builder request) {
        String authString = this.username + ":" + this.password;
        request.header("Authorization",
            "Basic " + Base64.getEncoder().encodeToString(authString.getBytes(StandardCharsets.UTF_8)));
    }
}
