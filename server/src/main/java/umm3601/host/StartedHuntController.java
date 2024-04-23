package umm3601.host;

import static com.mongodb.client.model.Filters.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import org.bson.UuidRepresentation;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.mongojack.JacksonMongoCollection;

import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.result.DeleteResult;

import io.javalin.Javalin;
import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import io.javalin.http.NotFoundResponse;
import umm3601.Controller;

public class StartedHuntController implements Controller {

  private static final String API_HUNT = "/api/hunts/{id}";
  private static final String API_START_HUNT = "/api/startHunt/{id}";
  private static final String API_STARTED_HUNT = "/api/startedHunts/{accessCode}";
  private static final String API_END_HUNT = "/api/endHunt/{id}";
  private static final String API_ENDED_HUNT = "/api/endedHunts/{id}";
  private static final String API_DELETE_HUNT = "/api/endedHunts/{id}";

  private static final int ACCESS_CODE_MIN = 100000;
  private static final int ACCESS_CODE_RANGE = 900000;
  private static final int ACCESS_CODE_LENGTH = 6;

  static final String HUNT_KEY = "huntId";

  private final JacksonMongoCollection<StartedHunt> startedHuntCollection;
  private final JacksonMongoCollection<Hunt> huntCollection;
  private final JacksonMongoCollection<Task> taskCollection;

  public StartedHuntController(MongoDatabase database) {
    startedHuntCollection = JacksonMongoCollection.builder().build(
        database,
        "startedHunts",
        StartedHunt.class,
        UuidRepresentation.STANDARD);

    File directory = new File("photos");
    if (!directory.exists()) {
        directory.mkdir();
    }
  }

  public void startHunt(Context ctx) {
    CompleteHunt completeHunt = new CompleteHunt();
    completeHunt.hunt = getHunt(ctx);
    completeHunt.tasks = getTasks(ctx);

    StartedHunt startedHunt = new StartedHunt();
    Random random = new Random();
    int accessCode = ACCESS_CODE_MIN + random.nextInt(ACCESS_CODE_RANGE); // Generate a random 6-digit number
    startedHunt.accessCode = String.format("%06d", accessCode); // Convert the number to a string
    startedHunt.completeHunt = completeHunt; // Assign the completeHunt to the startedHunt
    startedHunt.status = true; // true means the hunt is active
    startedHunt.endDate = null; // null endDate until the hunt is ended
    // Insert the StartedHunt into the startedHunt collection
    startedHuntCollection.insertOne(startedHunt);

    ctx.json(startedHunt.accessCode);
    ctx.status(HttpStatus.CREATED);
  }

  public Hunt getHunt(Context ctx) {
    String id = ctx.pathParam("id");
    Hunt hunt;

    try {
      hunt = huntCollection.find(eq("_id", new ObjectId(id))).first();
    } catch (IllegalArgumentException e) {
      throw new BadRequestResponse("The requested hunt id wasn't a legal Mongo Object ID.");
    }
    if (hunt == null) {
      throw new NotFoundResponse("The requested hunt was not found");
    } else {
      return hunt;
    }
  }

  public ArrayList<Task> getTasks(Context ctx) {
    Bson sortingOrder = constructSortingOrderTasks(ctx);

    String targetHunt = ctx.pathParam("id");

    ArrayList<Task> matchingTasks = taskCollection
        .find(eq(HUNT_KEY, targetHunt))
        .sort(sortingOrder)
        .into(new ArrayList<>());

    return matchingTasks;
  }

  private Bson constructSortingOrderTasks(Context ctx) {
    String sortBy = Objects.requireNonNullElse(ctx.queryParam("sortby"), "name");
    Bson sortingOrder = Sorts.ascending(sortBy);
    return sortingOrder;
  }

  public void getEndedHunts(Context ctx) {
    List<StartedHunt> endedHunts = startedHuntCollection.find(eq("status", false)).into(new ArrayList<>());
    ctx.json(endedHunts);
    ctx.status(HttpStatus.OK);
  }

  public void endStartedHunt(Context ctx) {
    String id = ctx.pathParam("id");
    StartedHunt startedHunt = startedHuntCollection.find(eq("_id", new ObjectId(id))).first();

    if (startedHunt == null) {
      throw new NotFoundResponse("The requested started hunt was not found.");
    } else {
      startedHunt.status = false;
      startedHunt.accessCode = "1";
      startedHunt.endDate = new Date();
      startedHuntCollection.save(startedHunt);
      ctx.status(HttpStatus.OK);
    }
  }

  public void deleteStartedHunt(Context ctx) {
    String id = ctx.pathParam("id");
    StartedHunt startedHunt = startedHuntCollection.find(eq("_id", new ObjectId(id))).first();
    DeleteResult deleteResult = startedHuntCollection.deleteOne(eq("_id", new ObjectId(id)));
    if (deleteResult.getDeletedCount() != 1) {
      ctx.status(HttpStatus.NOT_FOUND);
      throw new NotFoundResponse(
          "Was unable to delete ID "
              + id
              + "; perhaps illegal ID or an ID for an item not in the system?");
     }
    ctx.status(HttpStatus.OK);

    for (Task task : startedHunt.completeHunt.tasks) {
      for (String photo : task.photos) {
        deletePhoto(photo, ctx);
      }
    }
  }

  public void deletePhoto(String id, Context ctx) {
    Path filePath = Path.of("photos/" + id);
    if (!Files.exists(filePath)) {
      ctx.status(HttpStatus.NOT_FOUND);
      throw new BadRequestResponse("Photo with ID " + id + " does not exist");
  }

    try {
      Files.delete(filePath);

      ctx.status(HttpStatus.OK);
    } catch (IOException e) {
      ctx.status(HttpStatus.INTERNAL_SERVER_ERROR);
      throw new BadRequestResponse("Error deleting the photo: " + e.getMessage());
    }
  }

  @Override
  public void addRoutes(Javalin server) {
    server.get(API_HUNT, this::getCompleteHunt);
    server.get(API_START_HUNT, this::startHunt);
    server.get(API_STARTED_HUNT, this::getStartedHunt);
    server.put(API_END_HUNT, this::endStartedHunt);
    server.get(API_ENDED_HUNTS, this::getEndedHunts);
    server.delete(API_DELETE_HUNT, this::deleteStartedHunt);
  }
}
