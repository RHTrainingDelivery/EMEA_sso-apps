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

import io.quarkus.oidc.OidcConfigurationMetadata;
import io.quarkus.oidc.UserInfo;
import io.quarkus.security.Authenticated;
import io.quarkus.security.identity.SecurityIdentity;

@Path("/hello")
@Authenticated
public class GreetingResource {
    @Inject
    SecurityIdentity securityIdentity;

    @Inject
    OidcConfigurationMetadata provider;

    @Inject
    UserInfo userInfo;

    @Inject
    JsonWebToken accessToken;

    @GET
    @Path("/languages")
    @Produces(MediaType.APPLICATION_JSON)
    @PermitAll
    public String getLanguages() {
        return "[\"en\", \"es\", \"fr\"]";
    }

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
    public String whoAmI() {
        return this.securityIdentity.getPrincipal().getName() + " " +
                this.securityIdentity.getRoles().toString() + ": " +
                (this.accessToken != null ?
                    "token issued by " + accessToken.getIssuer() + " " +
                    "on " + accessToken.getIssuedAtTime() + " " +
                    "for " + accessToken.getSubject() + " " +
                    "until " + accessToken.getExpirationTime() + " " +
                    "with claims " + accessToken.getClaimNames() : "no JWT");
    }

    @GET
    @Path("/oidc")
    @Produces(MediaType.TEXT_PLAIN)
    public String getProviderInfo() {
        return "issuer: " + this.provider.getIssuer() + ",\n" +
                "auth at: " + this.provider.getAuthorizationUri() + ",\n" +
                "introspect at: " + this.provider.getIntrospectionUri() + ",\n" +
                "end session at: " + this.provider.getEndSessionUri() + ",\n" +
                "web key set at: " + this.provider.getJsonWebKeySetUri() + ",\n" +
                "token at: " + this.provider.getTokenUri() + ",\n" +
                "userinfo at: " + this.provider.getUserInfoUri() + ",\n" +
                "supports: " + this.provider.getSupportedScopes().toString();
    }

    @GET
    @Path("/userinfo")
    @Produces(MediaType.APPLICATION_JSON)
    public String getUserInfo() {
        return this.userInfo.getUserInfoString();
    }

    @GET
    @Path("/jwt")
    @Produces(MediaType.TEXT_PLAIN)
    public String getJwt() {
        return this.accessToken.getRawToken();
    }
}
