package umm3601.host;

import static com.mongodb.client.model.Filters.*;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

import org.bson.Document;
import org.bson.UuidRepresentation;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.mongojack.JacksonMongoCollection;

import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Sorts;

import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import io.javalin.http.NotFoundResponse;
import umm3601.Controller;

public class TaskController implements Controller {

  private static final String API_TASK = "/api/tasks/{id}";
  private static final String API_TASKS = "/api/tasks";

  static final String HOST_KEY = "hostId";
  static final String HUNT_KEY = "huntId";

  static final int REASONABLE_NAME_LENGTH_HUNT = 50;
  static final int REASONABLE_DESCRIPTION_LENGTH_HUNT = 200;

  static final int REASONABLE_NAME_LENGTH_TASK = 150;

  private final JacksonMongoCollection<Hunt> huntCollection;
  private final JacksonMongoCollection<Task> taskCollection;

  public TaskController(MongoDatabase database) {
    taskCollection = JacksonMongoCollection.builder().build(
        database,
        "tasks",
        Task.class,
        UuidRepresentation.STANDARD);

    huntCollection = JacksonMongoCollection.builder().build(
        database,
        "hunts",
        Hunt.class,
        UuidRepresentation.STANDARD);
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

  @Override
  public void addRoutes(Javalin server) {
    server.get(API_TASKS, this::getTasks);
    server.post(API_TASKS, this::addNewTask);
    server.delete(API_TASK, this::deleteTasks);
  }
}
