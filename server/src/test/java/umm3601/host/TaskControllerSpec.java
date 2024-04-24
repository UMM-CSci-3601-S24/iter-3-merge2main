package umm3601.host;

import static com.mongodb.client.model.Filters.eq;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.mongodb.MongoClientSettings;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import io.javalin.http.NotFoundResponse;
import io.javalin.json.JavalinJackson;
import io.javalin.validation.BodyValidator;
import io.javalin.validation.ValidationException;

@SuppressWarnings({ "MagicNumber" })
class TaskControllerSpec {
  private TaskController taskController;
  private ObjectId frysId;
  private ObjectId huntId;
  private ObjectId taskId;
  private ObjectId startedHuntId;

  private static MongoClient mongoClient;
  private static MongoDatabase db;
  private static JavalinJackson javalinJackson = new JavalinJackson();

  @Mock
  private Context ctx;

  @Captor
  private ArgumentCaptor<ArrayList<Hunt>> huntArrayListCaptor;

  @Captor
  private ArgumentCaptor<ArrayList<Task>> taskArrayListCaptor;

  @Captor
  private ArgumentCaptor<Host> hostCaptor;

  @Captor
  private ArgumentCaptor<CompleteHunt> completeHuntCaptor;

  @Captor
  private ArgumentCaptor<StartedHunt> startedHuntCaptor;

  @Captor
  private ArgumentCaptor<ArrayList<StartedHunt>> startedHuntArrayListCaptor;

  @Captor
  private ArgumentCaptor<EndedHunt> finishedHuntCaptor;

  @Captor
  private ArgumentCaptor<Map<String, String>> mapCaptor;

  @BeforeAll
  static void setupAll() {
    String mongoAddr = System.getenv().getOrDefault("MONGO_ADDR", "localhost");

    mongoClient = MongoClients.create(
        MongoClientSettings.builder()
            .applyToClusterSettings(builder -> builder.hosts(Arrays.asList(new ServerAddress(mongoAddr))))
            .build());
    db = mongoClient.getDatabase("test");
  }

  @AfterAll
  static void teardown() {
    db.drop();
    mongoClient.close();
  }

  @BeforeEach
  void setupEach() throws IOException {
    MockitoAnnotations.openMocks(this);

    MongoCollection<Document> taskDocuments = db.getCollection("tasks");
    taskDocuments.drop();
    List<Document> testTasks = new ArrayList<>();
    testTasks.add(
        new Document()
            .append("huntId", huntId.toHexString())
            .append("name", "Take a picture of a cat")
            .append("status", false)
            .append("photos", new ArrayList<String>()));
    testTasks.add(
        new Document()
            .append("huntId", huntId.toHexString())
            .append("name", "Take a picture of a dog")
            .append("status", false)
            .append("photos", new ArrayList<String>()));
    testTasks.add(
        new Document()
            .append("huntId", huntId.toHexString())
            .append("name", "Take a picture of a park")
            .append("status", true)
            .append("photos", new ArrayList<String>()));
    testTasks.add(
        new Document()
            .append("huntId", "differentId")
            .append("name", "Take a picture of a moose")
            .append("status", true)
            .append("photos", new ArrayList<String>()));

    taskId = new ObjectId();
    Document task = new Document()
        .append("_id", taskId)
        .append("huntId", "someId")
        .append("name", "Best Task")
        .append("status", false)
        .append("photos", new ArrayList<String>());

    taskDocuments.insertMany(testTasks);
    taskDocuments.insertOne(task);
  }

  @Test
  void addRoutes() {
    Javalin mockServer = mock(Javalin.class);
    taskController.addRoutes(mockServer);
    verify(mockServer, Mockito.atLeast(1)).get(any(), any());
  }

  @Test
  void getTasksByHuntId() throws IOException {
    String id = huntId.toHexString();
    when(ctx.pathParam("id")).thenReturn(id);

    ArrayList<Task> tasks = taskController.getTasks(ctx);

    assertEquals(3, tasks.size());
    for (Task task : tasks) {
      assertEquals(huntId.toHexString(), task.huntId);
    }
  }

  @Test
  void increaseTaskCount() throws IOException {
    String testHuntId = huntId.toHexString();
    assertEquals(3, db.getCollection("hunts").find(eq("_id", new ObjectId(testHuntId))).first().get("numberOfTasks"));

    taskController.increaseTaskCount(testHuntId);

    Document hunt = db.getCollection("hunts").find(eq("_id", new ObjectId(testHuntId))).first();
    assertEquals(4, hunt.get("numberOfTasks"));
  }

  @Test
  void decreaseTaskCount() throws IOException {
    String testHuntId = huntId.toHexString();
    assertEquals(3, db.getCollection("hunts").find(eq("_id", new ObjectId(testHuntId))).first().get("numberOfTasks"));

    taskController.decreaseTaskCount(testHuntId);

    Document hunt = db.getCollection("hunts").find(eq("_id", new ObjectId(testHuntId))).first();
    assertEquals(2, hunt.get("numberOfTasks"));
  }

  @Test
  void addTask() throws IOException {
    String testNewTask = """
        {
          "huntId": "bestHuntId",
          "name": "New Task",
          "status": false
        }
        """;
    when(ctx.bodyValidator(Task.class))
        .then(value -> new BodyValidator<Task>(testNewTask, Task.class, javalinJackson));

    taskController.addNewTask(ctx);
    verify(ctx).json(mapCaptor.capture());

    verify(ctx).status(HttpStatus.CREATED);

    Document addedTask = db.getCollection("tasks")
        .find(eq("_id", new ObjectId(mapCaptor.getValue().get("id")))).first();

    assertNotEquals("", addedTask.get("_id"));
    assertEquals("New Task", addedTask.get("name"));
    assertEquals("bestHuntId", addedTask.get("huntId"));
    assertEquals(false, addedTask.get("status"));
    assertEquals(new ArrayList<String>(), addedTask.get("photos"));
  }

  @Test
  void addInvalidHuntIdTask() throws IOException {
    String testNewTask = """
        {
          "huntId": "",
          "name": "New Task",
          "status": false
        }
        """;
    when(ctx.bodyValidator(Task.class))
        .then(value -> new BodyValidator<Task>(testNewTask, Task.class, javalinJackson));

    assertThrows(ValidationException.class, () -> {
      taskController.addNewTask(ctx);
    });
  }

  @Test
  void addInvalidHuntIdNullTask() throws IOException {
    String testNewTask = """
        {
          "huntId": null,
          "name": "New Task",
          "status": false
        }
        """;
    when(ctx.bodyValidator(Task.class))
        .then(value -> new BodyValidator<Task>(testNewTask, Task.class, javalinJackson));

    assertThrows(ValidationException.class, () -> {
      taskController.addNewTask(ctx);
    });
  }

  @Test
  void addInvalidNoNameTask() throws IOException {
    String testNewTask = """
        {
          "huntId": "bestHuntId",
          "name": "",
          "status": false
        }
        """;
    when(ctx.bodyValidator(Task.class))
        .then(value -> new BodyValidator<Task>(testNewTask, Task.class, javalinJackson));

    assertThrows(ValidationException.class, () -> {
      taskController.addNewTask(ctx);
    });
  }

  @Test
  void addInvalidLongNameTask() throws IOException {
    String tooLong = "t".repeat(HostController.REASONABLE_NAME_LENGTH_TASK + 1);
    String testNewTask = String.format("""
        {
          "huntId": "bestHuntId",
          "name": "%s",
          "status": false
        }
        """, tooLong);
    when(ctx.bodyValidator(Task.class))
        .then(value -> new BodyValidator<Task>(testNewTask, Task.class, javalinJackson));

    assertThrows(ValidationException.class, () -> {
      taskController.addNewTask(ctx);
    });
  }

  @Test
  void addInvalidStatusTask() throws IOException {
    String testNewTask = """
        {
          "huntId": "bestHuntId",
          "name": "",
          "status": null
        }
        """;
    when(ctx.bodyValidator(Task.class))
        .then(value -> new BodyValidator<Task>(testNewTask, Task.class, javalinJackson));

    assertThrows(ValidationException.class, () -> {
      taskController.addNewTask(ctx);
    });
  }

  @Test
  void deleteFoundTask() throws IOException {
    String testID = taskId.toHexString();
    when(ctx.pathParam("id")).thenReturn(testID);

    assertEquals(1, db.getCollection("tasks").countDocuments(eq("_id", new ObjectId(testID))));

    taskController.deleteTask(ctx);

    verify(ctx).status(HttpStatus.OK);

    assertEquals(0, db.getCollection("tasks").countDocuments(eq("_id", new ObjectId(testID))));
  }

  @Test
  void tryToDeleteNotFoundTask() throws IOException {
    String testID = taskId.toHexString();
    when(ctx.pathParam("id")).thenReturn(testID);

    taskController.deleteTask(ctx);
    assertEquals(0, db.getCollection("tasks").countDocuments(eq("_id", new ObjectId(testID))));

    assertThrows(NotFoundResponse.class, () -> {
      taskController.deleteTask(ctx);
    });

    verify(ctx).status(HttpStatus.NOT_FOUND);
    assertEquals(0, db.getCollection("tasks").countDocuments(eq("_id", new ObjectId(testID))));
  }
}
