package umm3601.host;

import io.javalin.Javalin;
import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import io.javalin.http.NotFoundResponse;
import io.javalin.websocket.WsContext;
import umm3601.Controller;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.bson.Document;
import org.bson.UuidRepresentation;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.mongojack.JacksonMongoCollection;

import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.result.DeleteResult;
import java.util.Base64;

public class HostController implements Controller {

  private static final String API_HOST = "/api/hosts/{id}";
  private static final String API_HUNT = "/api/hunts/{id}";
  private static final String API_HUNTS = "/api/hunts";
  private static final String API_TASK = "/api/tasks/{id}";
  private static final String API_TASKS = "/api/tasks";
  private static final String API_START_HUNT = "/api/startHunt/{id}";
  private static final String API_STARTED_HUNT = "/api/startedHunts/{accessCode}";
  private static final String API_END_HUNT = "/api/endHunt/{id}";
  private static final String API_ENDED_HUNT = "/api/endedHunts/{id}";
  private static final String API_ENDED_HUNTS = "/api/hosts/{id}/endedHunts";
  private static final String API_DELETE_HUNT = "/api/endedHunts/{id}";
  private static final String API_PHOTO_UPLOAD = "/api/startedHunt/{startedHuntId}/tasks/{taskId}/photo";
  private static final String API_PHOTO_REPLACE = "/api/startedHunt/{startedHuntId}/tasks/{taskId}/photo/{photoId}";
  private static final String WEBSOCKET_HOST = "/ws/host";
  private static final String API_PHOTO = "/api/photo/{filename}";


  static final String HOST_KEY = "hostId";
  static final String HUNT_KEY = "huntId";

  static final int REASONABLE_NAME_LENGTH_HUNT = 50;
  static final int REASONABLE_DESCRIPTION_LENGTH_HUNT = 200;
  private static final int REASONABLE_EST_LENGTH_HUNT = 240;

  static final int REASONABLE_NAME_LENGTH_TASK = 150;

  private static final int ACCESS_CODE_MIN = 100000;
  private static final int ACCESS_CODE_RANGE = 900000;
  private static final int ACCESS_CODE_LENGTH = 6;

  private static final int WEB_SOCKET_PING_INTERVAL = 15;

  private final JacksonMongoCollection<Host> hostCollection;
  private final JacksonMongoCollection<Hunt> huntCollection;
  private final JacksonMongoCollection<Task> taskCollection;
  private final JacksonMongoCollection<StartedHunt> startedHuntCollection;

  private HashSet<WsContext> connectedContexts = new HashSet<>();

  public HostController(MongoDatabase database) {
    hostCollection = JacksonMongoCollection.builder().build(
        database,
        "hosts",
        Host.class,
        UuidRepresentation.STANDARD);

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

    File directory = new File("photos");
    if (!directory.exists()) {
      directory.mkdir();
    }
    // If you require it to make the entire directory path including parents,
    // use directory.mkdirs(); here instead.

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

  public void getPhoto(Context ctx) {
  String filename = ctx.pathParam("filename");
  File file = new File("photos/" + filename);
  if (file.exists()) {
    try {
      ctx.result(new FileInputStream(file));
    } catch (FileNotFoundException e) {
      ctx.status(500).result("Error reading file: " + e.getMessage());
    }
  } else {
    ctx.status(404).result("Photo not found");
  }
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

  public StartedHunt getStartedHunt(Context ctx) {
    String accessCode = ctx.pathParam("accessCode");
    StartedHunt startedHunt;

    // Validate the access code
    if (accessCode.length() != ACCESS_CODE_LENGTH || !accessCode.matches("\\d+")) {
      throw new BadRequestResponse("The requested access code is not a valid access code.");
    }

    startedHunt = startedHuntCollection.find(eq("accessCode", accessCode)).first();

    if (startedHunt == null) {
      throw new NotFoundResponse("The requested access code was not found.");
    } else if (!startedHunt.status) {
      throw new BadRequestResponse("The requested hunt is no longer joinable.");
    } else {
      ctx.json(startedHunt);
      ctx.status(HttpStatus.OK);
      return startedHunt;
    }
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

  public void addPhoto(Context ctx) {
    String id = uploadPhoto(ctx);
    addPhotoPathToTask(ctx, id);
    ctx.status(HttpStatus.CREATED);
    ctx.json(Map.of("id", id));
  }

  public String getFileExtension(String filename) {
    int dotIndex = filename.lastIndexOf('.');
    if (dotIndex >= 0) {
      return filename.substring(dotIndex + 1);
    } else {
      return "";
    }
  }

  public String uploadPhoto(Context ctx) {
    try {
      var uploadedFile = ctx.uploadedFile("photo");
      if (uploadedFile != null) {
        try (InputStream in = uploadedFile.content()) {

          String id = UUID.randomUUID().toString();

          String extension = getFileExtension(uploadedFile.filename());
          File file = Path.of("photos", id + "." + extension).toFile();
          System.err.println("The path was " + file.toPath());

          Files.copy(in, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
          ctx.status(HttpStatus.OK);
          createAndSendEvent("photo-uploaded", id + "." + extension);
          return id + "." + extension;
        } catch (IOException e) {
          System.err.println("Error copying the uploaded file: " + e);
          throw new BadRequestResponse("Error handling the uploaded file: " + e.getMessage());
        }
      } else {
        throw new BadRequestResponse("No photo uploaded");
      }
    } catch (Exception e) {
      throw new BadRequestResponse("Unexpected error during photo upload: " + e.getMessage());
    }
  }

  public void addPhotoPathToTask(Context ctx, String photoPath) {
    String taskId = ctx.pathParam("taskId");
    String startedHuntId = ctx.pathParam("startedHuntId");
    StartedHunt startedHunt = startedHuntCollection.find(eq("_id", new ObjectId(startedHuntId))).first();
    if (startedHunt == null) {
      ctx.status(HttpStatus.NOT_FOUND);
      throw new BadRequestResponse("StartedHunt with ID " + startedHuntId + " does not exist");
    }

    Task task = startedHunt.completeHunt.tasks.stream().filter(t -> t._id.equals(taskId)).findFirst().orElse(null);

    if (task == null) {
      ctx.status(HttpStatus.NOT_FOUND);
      throw new BadRequestResponse("Task with ID " + taskId + " does not exist");
    }

    task.photos.add(photoPath);
    startedHunt.completeHunt.tasks.set(startedHunt.completeHunt.tasks.indexOf(task), task);
    startedHuntCollection.save(startedHunt);
  }

  public void replacePhoto(Context ctx) {
    String startedHuntId = ctx.pathParam("startedHuntId");
    String taskId = ctx.pathParam("taskId");
    String photoId = ctx.pathParam("photoId");
    deletePhoto(photoId, ctx);
    removePhotoPathFromTask(ctx, taskId, startedHuntId, photoId);
    addPhoto(ctx);
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

  public void removePhotoPathFromTask(Context ctx, String taskId, String startedHuntId, String photoId) {
    StartedHunt startedHunt = startedHuntCollection.find(eq("_id", new ObjectId(startedHuntId))).first();
    if (startedHunt == null) {
      ctx.status(HttpStatus.NOT_FOUND);
      throw new BadRequestResponse("StartedHunt with ID " + startedHuntId + " does not exist");
    }

    Task task = startedHunt.completeHunt.tasks.stream().filter(t -> t._id.equals(taskId)).findFirst().orElse(null);
    if (task == null) {
      ctx.status(HttpStatus.NOT_FOUND);
      throw new BadRequestResponse("Task with ID " + taskId + " does not exist");
    }

    task.photos.remove(photoId);
    startedHunt.completeHunt.tasks.set(startedHunt.completeHunt.tasks.indexOf(task), task);
    startedHuntCollection.save(startedHunt);
  }

  public void getEndedHunt(Context ctx) {
    EndedHunt finishedHunt = new EndedHunt();
    finishedHunt.startedHunt = getStartedHuntById(ctx);
    finishedHunt.finishedTasks = getFinishedTasks(finishedHunt.startedHunt.completeHunt.tasks);

    ctx.json(finishedHunt);
    ctx.status(HttpStatus.OK);
  }

  public StartedHunt getStartedHuntById(Context ctx) {
    String id = ctx.pathParam("id");
    StartedHunt startedHunt;

    try {
      startedHunt = startedHuntCollection.find(eq("_id", new ObjectId(id))).first();
    } catch (IllegalArgumentException e) {
      throw new BadRequestResponse("The requested started hunt id wasn't a legal Mongo Object ID.");
    }
    if (startedHunt == null) {
      throw new NotFoundResponse("The requested started hunt was not found");
    } else {
      return startedHunt;
    }
  }

  public List<FinishedTask> getFinishedTasks(List<Task> tasks) {
    ArrayList<FinishedTask> finishedTasks = new ArrayList<>();
    FinishedTask finishedTask;
    for (Task task : tasks) {
      finishedTask = new FinishedTask();
      finishedTask.taskId = task._id;
      finishedTask.photos = getPhotosFromTask(task);
      finishedTasks.add(finishedTask);
    }
    return finishedTasks;
  }

  public List<String> getPhotosFromTask(Task task) {
    List<String> encodedPhotos = new ArrayList<>();
    for (String photoPath : task.photos) {
      File photo = new File("photos/" + photoPath);
      if (photo.exists()) {
        try {
          byte[] bytes = Files.readAllBytes(Paths.get(photo.getPath()));
          String encoded = "data:image/png;base64," + Base64.getEncoder().encodeToString(bytes);
          encodedPhotos.add(encoded);
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
    return encodedPhotos;
  }

  public Map<String, String> createEvent(String event, String data) {
    return Map.of(event, data, "timestamp", new Date().toString());
  }

  public void updateListeners(Map<String, String> events) {
    Iterator<WsContext> iterator = connectedContexts.iterator();
    while (iterator.hasNext()) {
      WsContext ws = iterator.next();
      if (ws.session.isOpen()) {
        ws.send(events);
      } else {
        iterator.remove();
      }
    }
  }

  public void addConnectedContext(WsContext context) {
    System.out.println("Adding connected context: " + context);
    this.connectedContexts.add(context);
  }

  public ArrayList<WsContext> getConnectedContexts() {
    System.out.println("Getting connected contexts");
    return new ArrayList<>(this.connectedContexts);
  }
  public void createAndSendEvent(String event, String data) {
    System.out.println("Creating and sending event: " + event);
    Map<String, String> events = createEvent(event, data);
    System.out.println("Sending event: " + events);
    updateListeners(events);
  }

  public void handleWebSocketConnections(Javalin server) {
    System.out.println("Handling WebSocket connections");
    server.ws(WEBSOCKET_HOST, ws -> {
      ws.onConnect(ctx -> {
        System.out.println("WebSocket connected: " + ctx);
        addConnectedContext(ctx);
        ctx.enableAutomaticPings(WEB_SOCKET_PING_INTERVAL, TimeUnit.SECONDS);

      });
    });
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
    server.get(API_STARTED_HUNT, this::getStartedHunt);
    server.put(API_END_HUNT, this::endStartedHunt);
    server.post(API_PHOTO_UPLOAD, this::addPhoto);
    server.put(API_PHOTO_REPLACE, this::replacePhoto);
    server.get(API_ENDED_HUNT, this::getEndedHunt);
    server.get(API_ENDED_HUNTS, this::getEndedHunts);
    server.delete(API_DELETE_HUNT, this::deleteStartedHunt);
    server.get(API_PHOTO, this::getPhoto);


    handleWebSocketConnections(server);
  }
}
