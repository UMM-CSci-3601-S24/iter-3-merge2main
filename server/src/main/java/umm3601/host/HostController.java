package umm3601.host;

import static com.mongodb.client.model.Filters.eq;

import org.bson.UuidRepresentation;
import org.bson.types.ObjectId;
import org.mongojack.JacksonMongoCollection;

import com.mongodb.client.MongoDatabase;

import io.javalin.Javalin;
import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import io.javalin.http.NotFoundResponse;
import umm3601.Controller;

public class HostController implements Controller {

  private static final String API_HOST = "/api/hosts/{id}";

  static final String HOST_KEY = "hostId";
  static final String HUNT_KEY = "huntId";

  static final int REASONABLE_NAME_LENGTH_HUNT = 50;
  static final int REASONABLE_DESCRIPTION_LENGTH_HUNT = 200;

  static final int REASONABLE_NAME_LENGTH_TASK = 150;

  private final JacksonMongoCollection<Host> hostCollection;

  public HostController(MongoDatabase database) {
    hostCollection = JacksonMongoCollection.builder().build(
        database,
        "hosts",
        Host.class,
        UuidRepresentation.STANDARD);
  }

  public void getHost(Context ctx) {
    String id = ctx.pathParam("id");
    Host host;

    try {
      host = hostCollection.find(eq("_id", new ObjectId(id))).first();
    } catch (IllegalArgumentException e) {
      throw new BadRequestResponse("The requested host id wasn't a legal Mongo Object ID.");
    }
    if (host == null) {
      throw new NotFoundResponse("The requested host was not found");
    } else {
      ctx.json(host);
      ctx.status(HttpStatus.OK);
    }
  }

  @Override
  public void addRoutes(Javalin server) {
    server.get(API_HOST, this::getHost);
  }
}
