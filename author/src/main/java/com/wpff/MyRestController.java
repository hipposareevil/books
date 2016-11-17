package com.wpff;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Set;
 
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;


@Path("/foo")
@Produces(MediaType.APPLICATION_JSON)
public class MyRestController {

  private final Validator validator;

  public MyRestController(Validator validator) {
    this.validator = validator;
  }

  @GET
  public Response getFoo() {
    String[] foo = new String[2];
    foo[0] = "hi"; foo[1] = "by";
    return Response.ok(foo).build();
  }

  @GET
  @Path("/bar")
  public Response getBar() {
    String[] foo = new String[2];
    foo[0] = "hi"; foo[1] = "byeeee";
    return Response.ok(foo).build();
  }

}
