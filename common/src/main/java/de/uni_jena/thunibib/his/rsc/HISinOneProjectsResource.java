package de.uni_jena.thunibib.his.rsc;

import de.uni_jena.thunibib.his.api.client.HISInOneClient;
import de.uni_jena.thunibib.his.api.client.HISinOneClientFactory;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mycore.frontend.jersey.access.MCRRequireLogin;
import org.mycore.frontend.jersey.filter.access.MCRRestrictedAccess;

import java.util.HashMap;

@Path("/project")
public class HISinOneProjectsResource {
    private static final Logger LOGGER = LogManager.getLogger(HISinOneProjectsResource.class);

    @GET
    @MCRRestrictedAccess(MCRRequireLogin.class)
    @Produces(MediaType.APPLICATION_JSON)
    public Response searchProjects(@QueryParam("q") String q) {
        HashMap<String, String> p = new HashMap<>();
        p.put("q", q);

        try (HISInOneClient client = HISinOneClientFactory.create();
            Response resp = client.get("fs/res/project", p)) {
            String json = resp.readEntity(String.class);
            return Response.ok(json).build();
        }
    }

    @GET
    @MCRRestrictedAccess(MCRRequireLogin.class)
    @Path("{projectId:.+}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getProject(@PathParam("projectId") String projectId) {
        try (HISInOneClient client = HISinOneClientFactory.create();
            Response resp = client.get("fs/res/project/" + projectId)) {
            String json = resp.readEntity(String.class);
            return Response.ok(json).build();
        }
    }
}
