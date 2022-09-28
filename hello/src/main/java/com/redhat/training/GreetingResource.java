package com.redhat.training;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/hello")
public class GreetingResource {
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String hello(@HeaderParam("Accept-Language") String language) {
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
    public String helloFrench() {
        return "Bonjour!";
    }

    @GET
    @Path("/es")
    @Produces(MediaType.TEXT_PLAIN)
    public String helloSpanish() {
        return "¡Hola!";
    }

    @GET
    @Path("/en")
    @Produces(MediaType.TEXT_PLAIN)
    public String helloEnglish() {
        return "Hello!";
    }
}
