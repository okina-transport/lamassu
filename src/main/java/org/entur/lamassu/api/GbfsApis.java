package org.entur.lamassu.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.ws.rs.*;
import org.entur.gbfs.GbfsSubscriptionManager;
import org.springframework.stereotype.Controller;

@Controller
@Path("/gbfs")
@Tag(name = "GbfsApis", description = "API de Gbfs")
public class GbfsApis {

  private GbfsSubscriptionManager subscriptionManager;

  public GbfsApis() {}

  @POST
  @Path("/unsubscribe/{id}")
  @Operation(summary = "Unsubscribe")
  public void unsubscribe(@PathParam("id") String id) {
    subscriptionManager.unsubscribe(id);
  }
}
