package com.redhat.training;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.jwt.JsonWebToken;

import io.quarkus.security.Authenticated;
import io.quarkus.security.identity.SecurityIdentity;

@Path("/hello")
@Authenticated
public class GreetingResource {
    @Inject
    SecurityIdentity securityIdentity;

    @Inject
    JsonWebToken accessToken;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @PermitAll
    public String hello(@HeaderParam("Accept-Language") String language) {
        if (this.securityIdentity == null ||
            this.securityIdentity.getPrincipal() == null ||
            this.securityIdentity.getPrincipal().getName() == "") {
            return "unauthorised";
        }
        if (language == null || language == "") {
            language = "en";
        }
        switch (language) {
            case "en":
                return this.helloEnglish();
            case "fr":
                return this.helloFrench();
            case "es":
                return this.helloSpanish();
            default:
                return this.helloEnglish() + " (fallback)";
        }
    }

    @GET
    @Path("/fr")
    @Produces(MediaType.TEXT_PLAIN)
    @RolesAllowed("french")
    public String helloFrench() {
        return "Bonjour!";
    }

    @GET
    @Path("/es")
    @Produces(MediaType.TEXT_PLAIN)
    @RolesAllowed("spanish")
    public String helloSpanish() {
        return "¡Hola!";
    }

    @GET
    @Path("/en")
    @Produces(MediaType.TEXT_PLAIN)
    @RolesAllowed("english")
    public String helloEnglish() {
        return "Hello!";
    }

    @GET
    @Path("/whoami")
    @Produces(MediaType.TEXT_PLAIN)
    @PermitAll
    public String whoAmI() {
        return this.securityIdentity.getPrincipal().getName() + " " +
                this.securityIdentity.getRoles().toString() + ": " +
                (accessToken != null ?
                    "token issued by " + accessToken.getIssuer() + " on " + accessToken.getIssuedAtTime() + " " +
                    "for " + accessToken.getSubject() + " until " + accessToken.getExpirationTime() + " " +
                    "with claims " + accessToken.getClaimNames() : "no JWT");
    }
}
