package umm3601.host;

import static com.mongodb.client.model.Filters.eq;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

import org.bson.Document;
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

public class HuntController implements Controller {

  private static final String API_HUNT = "/api/hunts/{id}";
  private static final String API_HUNTS = "/api/hunts";
  private static final String API_HOST = "/api/hosts/{id}";
  private static final String API_TASK = "/api/tasks/{id}";
  private static final String API_TASKS = "/api/tasks";
  private static final String API_START_HUNT = "/api/startHunt/{id}";


  static final String HOST_KEY = "hostId";
  static final String HUNT_KEY = "huntId";

  static final int REASONABLE_NAME_LENGTH_HUNT = 50;
  static final int REASONABLE_DESCRIPTION_LENGTH_HUNT = 200;
  private static final int  REASONABLE_EST_LENGTH_HUNT = 240;

  static final int REASONABLE_NAME_LENGTH_TASK = 150;

  private static final int ACCESS_CODE_MIN = 100000;
  private static final int ACCESS_CODE_RANGE = 900000;

  private final JacksonMongoCollection<Hunt> huntCollection;
  private final JacksonMongoCollection<Task> taskCollection;
  private final JacksonMongoCollection<StartedHunt> startedHuntCollection;

  public HuntController(MongoDatabase database) {
    huntCollection = JacksonMongoCollection.builder().build(
      database,
      "hunts",
      Hunt.class,
      UuidRepresentation.STANDARD);

    taskCollection = JacksonMongoCollection.builder().build(
      database,
      "tasks",
      Task.class,
      UuidRepresentation.STANDARD);

    startedHuntCollection = JacksonMongoCollection.builder().build(
      database,
      "startedHunts",
      StartedHunt.class,
      UuidRepresentation.STANDARD);
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

  public void getHunts(Context ctx) {
    Bson combinedFilter = constructFilterHunts(ctx);
    Bson sortingOrder = constructSortingOrderHunts(ctx);

    ArrayList<Hunt> matchingHunts = huntCollection
        .find(combinedFilter)
        .sort(sortingOrder)
        .into(new ArrayList<>());

    ctx.json(matchingHunts);

    ctx.status(HttpStatus.OK);
  }

  private Bson constructFilterHunts(Context ctx) {
    List<Bson> filters = new ArrayList<>();

    if (ctx.queryParamMap().containsKey(HOST_KEY)) {
      String targetHost = ctx.queryParamAsClass(HOST_KEY, String.class).get();
      filters.add(eq(HOST_KEY, targetHost));
    }

    Bson combinedFilter = filters.isEmpty() ? new Document() : and(filters);

    return combinedFilter;
  }

  private Bson and(List<Bson> filters) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'and'");
  }

  private Bson constructSortingOrderHunts(Context ctx) {
    String sortBy = Objects.requireNonNullElse(ctx.queryParam("sortby"), "name");
    Bson sortingOrder = Sorts.ascending(sortBy);
    return sortingOrder;
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

  public void addNewHunt(Context ctx) {
    Hunt newHunt = ctx.bodyValidator(Hunt.class)
    .check(hunt -> hunt.hostId != null && hunt.hostId.length() > 0, "Invalid hostId")
    .check(hunt -> hunt.name.length() <= REASONABLE_NAME_LENGTH_HUNT, "Name must be less than 50 characters")
    .check(hunt -> hunt.name.length() > 0, "Name must be at least 1 character")
    .check(hunt -> hunt.description.length() <= REASONABLE_DESCRIPTION_LENGTH_HUNT,
     "Description must be less than 200 characters")
    .check(hunt -> hunt.est <= REASONABLE_EST_LENGTH_HUNT, "Estimated time must be less than 4 hours")
    .get();

    huntCollection.insertOne(newHunt);
    ctx.json(Map.of("id", newHunt._id));
    ctx.status(HttpStatus.CREATED);
  }

  public void addNewTask(Context ctx) {
    Task newTask = ctx.bodyValidator(Task.class)
    .check(task -> task.huntId != null && task.huntId.length() > 0, "Invalid huntId")
    .check(task -> task.name.length() <= REASONABLE_NAME_LENGTH_TASK, "Name must be less than 150 characters")
    .check(task -> task.name.length() > 0, "Name must be at least 1 character")
    .get();

    newTask.photos = new ArrayList<String>();

    taskCollection.insertOne(newTask);
    increaseTaskCount(newTask.huntId);
    ctx.json(Map.of("id", newTask._id));
    ctx.status(HttpStatus.CREATED);
  }

  public void increaseTaskCount(String huntId) {
    try {
      huntCollection.findOneAndUpdate(eq("_id", new ObjectId(huntId)),
          new Document("$inc", new Document("numberOfTasks", 1)));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void deleteHunt(Context ctx) {
    String id = ctx.pathParam("id");
    DeleteResult deleteResult = huntCollection.deleteOne(eq("_id", new ObjectId(id)));
    if (deleteResult.getDeletedCount() != 1) {
      ctx.status(HttpStatus.NOT_FOUND);
      throw new NotFoundResponse(
          "Was unable to delete ID "
              + id
              + "; perhaps illegal ID or an ID for an item not in the system?");
    }
    deleteTasks(ctx);
    ctx.status(HttpStatus.OK);
  }

  public void deleteTask(Context ctx) {
    String id = ctx.pathParam("id");
    try {
      String huntId = taskCollection.find(eq("_id", new ObjectId(id))).first().huntId;
      taskCollection.deleteOne(eq("_id", new ObjectId(id)));
      decreaseTaskCount(huntId);
    } catch (Exception e) {
      ctx.status(HttpStatus.NOT_FOUND);
      throw new NotFoundResponse(
          "Was unable to delete ID "
              + id
              + "; perhaps illegal ID or an ID for an item not in the system?");
    }
    ctx.status(HttpStatus.OK);
  }

  public void decreaseTaskCount(String huntId) {
    try {
      huntCollection.findOneAndUpdate(eq("_id", new ObjectId(huntId)),
          new Document("$inc", new Document("numberOfTasks", -1)));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void deleteTasks(Context ctx) {
    String huntId = ctx.pathParam("id");
    taskCollection.deleteMany(eq("huntId", huntId));
  }

  public void getCompleteHunt(Context ctx) {
    CompleteHunt completeHunt = new CompleteHunt();
    completeHunt.hunt = getHunt(ctx);
    completeHunt.tasks = getTasks(ctx);

    ctx.json(completeHunt);
    ctx.status(HttpStatus.OK);
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

  @Override
  public void addRoutes(Javalin server) {
    server.get(API_HOST, this::getHunts);
    server.get(API_HUNT, this::getCompleteHunt);
    server.post(API_HUNTS, this::addNewHunt);
    server.get(API_TASKS, this::getTasks);
    server.post(API_TASKS, this::addNewTask);
    server.delete(API_HUNT, this::deleteHunt);
    server.delete(API_TASK, this::deleteTask);
    server.get(API_START_HUNT, this::startHunt);
  }
}
