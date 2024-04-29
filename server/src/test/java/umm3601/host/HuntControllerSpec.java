package umm3601.host;

import static com.mongodb.client.model.Filters.eq;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
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
import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import io.javalin.http.NotFoundResponse;
import io.javalin.json.JavalinJackson;
import io.javalin.validation.BodyValidator;
import io.javalin.validation.ValidationException;
import io.javalin.validation.Validator;

@SuppressWarnings({ "MagicNumber" })
class HuntControllerSpec {
  private HuntController huntController;
  private ObjectId frysId;
  private ObjectId huntId;
  private ObjectId taskId;

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
  private ArgumentCaptor<CompleteHunt> completeHuntCaptor;

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

    MongoCollection<Document> hostDocuments = db.getCollection("hosts");
    hostDocuments.drop();
    frysId = new ObjectId();
    Document fry = new Document()
        .append("_id", frysId)
        .append("name", "Fry")
        .append("userName", "fry")
        .append("email", "fry@email");

    hostDocuments.insertOne(fry);

    MongoCollection<Document> huntDocuments = db.getCollection("hunts");
    huntDocuments.drop();
    List<Document> testHunts = new ArrayList<>();
    testHunts.add(
        new Document()
            .append("hostId", "frysId")
            .append("name", "Fry's Hunt")
            .append("description", "Fry's hunt for the seven leaf clover")
            .append("est", 20)
            .append("numberOfTasks", 5));
    testHunts.add(
        new Document()
            .append("hostId", "frysId")
            .append("name", "Fry's Hunt 2")
            .append("description", "Fry's hunt for Morris")
            .append("est", 30)
            .append("numberOfTasks", 2));
    testHunts.add(
        new Document()
            .append("hostId", "frysId")
            .append("name", "Fry's Hunt 3")
            .append("description", "Fry's hunt for money")
            .append("est", 40)
            .append("numberOfTasks", 1));
    testHunts.add(
        new Document()
            .append("hostId", "differentId")
            .append("name", "Different's Hunt")
            .append("description", "Different's hunt for money")
            .append("est", 60)
            .append("numberOfTasks", 10));
    huntId = new ObjectId();
    Document hunt = new Document()
            .append("_id", huntId)
            .append("hostId", "frysId")
            .append("name", "Best Hunt")
            .append("description", "This is the best hunt")
            .append("est", 20)
            .append("numberOfTasks", 3);

    huntDocuments.insertMany(testHunts);
    huntDocuments.insertOne(hunt);

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

    huntController = new HuntController(db);
  }

  @Test
  void addRoutes() {
    Javalin mockServer = mock(Javalin.class);
    huntController.addRoutes(mockServer);
    verify(mockServer, Mockito.atLeast(1)).get(any(), any());
  }

  @Test
  void getHuntsByHostId() throws IOException {
    Map<String, List<String>> queryParams = new HashMap<>();
    queryParams.put("hostId", Collections.singletonList("frysId"));
    when(ctx.queryParamMap()).thenReturn(queryParams);
    when(ctx.queryParamAsClass("hostId", String.class))
        .thenReturn(Validator.create(String.class, "frysId", "hostId"));

    huntController.getHunts(ctx);
    verify(ctx).json(huntArrayListCaptor.capture());
    verify(ctx).status(HttpStatus.OK);
    
    assertEquals(4, huntArrayListCaptor.getValue().size());
    for (Hunt hunt : huntArrayListCaptor.getValue()) {
      assertEquals("frysId", hunt.hostId);
    }
  }

  @Test
  void getHuntById() throws IOException {
    String id = huntId.toHexString();
    when(ctx.pathParam("id")).thenReturn(id);

    Hunt hunt = huntController.getHunt(ctx);

    assertEquals("Best Hunt", hunt.name);
    assertEquals(huntId.toHexString(), hunt._id);
  }

  @Test
  void getHuntWithBadId() throws IOException {
    when(ctx.pathParam("id")).thenReturn("bad");

    Throwable exception = assertThrows(BadRequestResponse.class, () -> {
      huntController.getHunt(ctx);
    });

    assertEquals("The requested hunt id wasn't a legal Mongo Object ID.", exception.getMessage());
  }

  @Test
  void getHuntWithNonexistentId() throws IOException {
    String id = "588935f5c668650dc77df581";
    when(ctx.pathParam("id")).thenReturn(id);

    Throwable exception = assertThrows(NotFoundResponse.class, () -> {
      huntController.getHunt(ctx);
    });

    assertEquals("The requested hunt was not found", exception.getMessage());
  }

  @Test
  void addHunt() throws IOException {
    String testNewHunt = """
        {
          "hostId": "frysId",
          "name": "New Hunt",
          "description": "Newly made hunt",
          "est": 45,
          "numberOfTasks": 3
        }
        """;
    when(ctx.bodyValidator(Hunt.class))
        .then(value -> new BodyValidator<Hunt>(testNewHunt, Hunt.class, javalinJackson));

    huntController.addNewHunt(ctx);
    verify(ctx).json(mapCaptor.capture());

    verify(ctx).status(HttpStatus.CREATED);

    Document addedHunt = db.getCollection("hunts")
        .find(eq("_id", new ObjectId(mapCaptor.getValue().get("id")))).first();

    assertNotEquals("", addedHunt.get("_id"));
    assertEquals("New Hunt", addedHunt.get("name"));
    assertEquals("frysId", addedHunt.get(HostController.HOST_KEY));
    assertEquals("Newly made hunt", addedHunt.get("description"));
    assertEquals(45, addedHunt.get("est"));
    assertEquals(3, addedHunt.get("numberOfTasks"));
  }

  @Test
  void addInvalidNoNameHunt() throws IOException {
    String testNewHunt = """
        {
          "hostId": "frysId",
          "name": "",
          "description": "Newly made hunt",
          "est": 45,
          "numberOfTasks": 3
        }
        """;
    when(ctx.bodyValidator(Hunt.class))
        .then(value -> new BodyValidator<Hunt>(testNewHunt, Hunt.class, javalinJackson));

    assertThrows(ValidationException.class, () -> {
      huntController.addNewHunt(ctx);
    });
  }

  @Test
  void addInvalidLongNameHunt() throws IOException {
    String testNewHunt = """
        {
          "hostId": "frysId",
          "name": "This should be a name that is too long to be valid hopefully",
          "description": "Newly made hunt",
          "est": 45,
          "numberOfTasks": 3
        }
        """;
    when(ctx.bodyValidator(Hunt.class))
        .then(value -> new BodyValidator<Hunt>(testNewHunt, Hunt.class, javalinJackson));

    assertThrows(ValidationException.class, () -> {
      huntController.addNewHunt(ctx);
    });
  }

  @Test
  void addInvalidHostIdHunt() throws IOException {
    String testNewHunt = """
        {
          "hostId": "",
          "name": "New Hunt",
          "description": "Newly made hunt",
          "est": 45,
          "numberOfTasks": 3
        }
        """;
    when(ctx.bodyValidator(Hunt.class))
        .then(value -> new BodyValidator<Hunt>(testNewHunt, Hunt.class, javalinJackson));

    assertThrows(ValidationException.class, () -> {
      huntController.addNewHunt(ctx);
    });
  }

  @Test
  void addInvalidHostNullIdHunt() throws IOException {
    String testNewHunt = """
        {
          "hostId": null,
          "name": "New Hunt",
          "description": "Newly made hunt",
          "est": 45,
          "numberOfTasks": 3
        }
        """;
    when(ctx.bodyValidator(Hunt.class))
        .then(value -> new BodyValidator<Hunt>(testNewHunt, Hunt.class, javalinJackson));

    assertThrows(ValidationException.class, () -> {
      huntController.addNewHunt(ctx);
    });
  }

  @Test
  void addInvalidDescriptionHunt() throws IOException {
    String tooLong = "t".repeat(HostController.REASONABLE_DESCRIPTION_LENGTH_HUNT + 1);
    String testNewHunt = String.format("""
        {
          "hostId": "frysId",
          "name": "New Hunt",
          "description": "%s",
          "est": 45,
          "numberOfTasks": 3
        }
        """, tooLong);
    when(ctx.bodyValidator(Hunt.class))
        .then(value -> new BodyValidator<Hunt>(testNewHunt, Hunt.class, javalinJackson));

    assertThrows(ValidationException.class, () -> {
      huntController.addNewHunt(ctx);
    });
  }

  @Test
  void addInvalidESTHunt() throws IOException {
    String testNewHunt = """
        {
          "hostId": "frysId",
          "name": "New Hunt",
          "description": "This is a great hunt",
          "est": 300,
          "numberOfTasks": 3
        }
        """;
    when(ctx.bodyValidator(Hunt.class))
        .then(value -> new BodyValidator<Hunt>(testNewHunt, Hunt.class, javalinJackson));

    assertThrows(ValidationException.class, () -> {
      huntController.addNewHunt(ctx);
    });
  }

  @Test
  void addHuntWithMaxEST() throws IOException {
    String testNewHunt = """
        {
          "hostId": "frysId",
          "name": "New Hunt",
          "description": "Newly made hunt",
          "est": 240,
          "numberOfTasks": 3
        }
        """;
    when(ctx.bodyValidator(Hunt.class))
        .then(value -> new BodyValidator<Hunt>(testNewHunt, Hunt.class, javalinJackson));

    huntController.addNewHunt(ctx);
    verify(ctx).json(mapCaptor.capture());

    verify(ctx).status(HttpStatus.CREATED);

    Document addedHunt = db.getCollection("hunts")
        .find(eq("_id", new ObjectId(mapCaptor.getValue().get("id")))).first();

    assertNotEquals("", addedHunt.get("_id"));
    assertEquals("New Hunt", addedHunt.get("name"));
    assertEquals("frysId", addedHunt.get(HostController.HOST_KEY));
    assertEquals("Newly made hunt", addedHunt.get("description"));
    assertEquals(240, addedHunt.get("est"));
    assertEquals(3, addedHunt.get("numberOfTasks"));
  }

  @Test
  void addInvalidNumberOfTasksHunt() throws IOException {
    String testNewHunt = """
        {
          "hostId": "frysId",
          "name": "New Hunt",
          "description": "This is a great hunt",
          "est": 30,
          "numberOfTasks":
        }
        """;
    when(ctx.bodyValidator(Hunt.class))
        .then(value -> new BodyValidator<Hunt>(testNewHunt, Hunt.class, javalinJackson));

    assertThrows(ValidationException.class, () -> {
      huntController.addNewHunt(ctx);
    });
  }

  @Test
  void getTasksByHuntId() throws IOException {
    String id = huntId.toHexString();
    when(ctx.pathParam("id")).thenReturn(id);

    ArrayList<Task> tasks = huntController.getTasks(ctx);

    assertEquals(3, tasks.size());
    for (Task task : tasks) {
      assertEquals(huntId.toHexString(), task.huntId);
    }
  }

  @Test
  void increaseTaskCount() throws IOException {
    String testHuntId = huntId.toHexString();
    assertEquals(3, db.getCollection("hunts").find(eq("_id", new ObjectId(testHuntId))).first().get("numberOfTasks"));

    huntController.increaseTaskCount(testHuntId);

    Document hunt = db.getCollection("hunts").find(eq("_id", new ObjectId(testHuntId))).first();
    assertEquals(4, hunt.get("numberOfTasks"));
  }

  @Test
  void decreaseTaskCount() throws IOException {
    String testHuntId = huntId.toHexString();
    assertEquals(3, db.getCollection("hunts").find(eq("_id", new ObjectId(testHuntId))).first().get("numberOfTasks"));

    huntController.decreaseTaskCount(testHuntId);

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

    huntController.addNewTask(ctx);
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
      huntController.addNewTask(ctx);
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
      huntController.addNewTask(ctx);
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
      huntController.addNewTask(ctx);
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
      huntController.addNewTask(ctx);
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
      huntController.addNewTask(ctx);
    });
  }

  @Test
  void deleteFoundHunt() throws IOException {
    String testID = huntId.toHexString();
    when(ctx.pathParam("id")).thenReturn(testID);

    assertEquals(1, db.getCollection("hunts").countDocuments(eq("_id", new ObjectId(testID))));

    huntController.deleteHunt(ctx);

    verify(ctx).status(HttpStatus.OK);

    assertEquals(0, db.getCollection("hunts").countDocuments(eq("_id", new ObjectId(testID))));
    assertEquals(0, db.getCollection("tasks").countDocuments(eq("_id", new ObjectId(testID))));
  }

  @Test
  void tryToDeleteNotFoundHunt() throws IOException {
    String testID = huntId.toHexString();
    when(ctx.pathParam("id")).thenReturn(testID);

    huntController.deleteHunt(ctx);
    assertEquals(0, db.getCollection("hunts").countDocuments(eq("_id", new ObjectId(testID))));

    assertThrows(NotFoundResponse.class, () -> {
      huntController.deleteHunt(ctx);
    });

    verify(ctx).status(HttpStatus.NOT_FOUND);
    assertEquals(0, db.getCollection("hunts").countDocuments(eq("_id", new ObjectId(testID))));
  }

  @Test
  void deleteFoundTask() throws IOException {
    String testID = taskId.toHexString();
    when(ctx.pathParam("id")).thenReturn(testID);

    assertEquals(1, db.getCollection("tasks").countDocuments(eq("_id", new ObjectId(testID))));

    huntController.deleteTask(ctx);

    verify(ctx).status(HttpStatus.OK);

    assertEquals(0, db.getCollection("tasks").countDocuments(eq("_id", new ObjectId(testID))));
  }

  @Test
  void tryToDeleteNotFoundTask() throws IOException {
    String testID = taskId.toHexString();
    when(ctx.pathParam("id")).thenReturn(testID);

    huntController.deleteTask(ctx);
    assertEquals(0, db.getCollection("tasks").countDocuments(eq("_id", new ObjectId(testID))));

    assertThrows(NotFoundResponse.class, () -> {
      huntController.deleteTask(ctx);
    });

    verify(ctx).status(HttpStatus.NOT_FOUND);
    assertEquals(0, db.getCollection("tasks").countDocuments(eq("_id", new ObjectId(testID))));
  }

  @Test
  void getCompleteHuntById() throws IOException {
    String id = huntId.toHexString();
    when(ctx.pathParam("id")).thenReturn(id);

    huntController.getCompleteHunt(ctx);

    verify(ctx).json(completeHuntCaptor.capture());
    verify(ctx).status(HttpStatus.OK);

    assertEquals("Best Hunt", completeHuntCaptor.getValue().hunt.name);
    assertEquals(huntId.toHexString(), completeHuntCaptor.getValue().hunt._id);

    assertEquals(3, completeHuntCaptor.getValue().tasks.size());
    for (Task task : completeHuntCaptor.getValue().tasks) {
      assertEquals(huntId.toHexString(), task.huntId);
    }
  }

  @Test
  void getCompleteHuntWithBadId() throws IOException {
    when(ctx.pathParam("id")).thenReturn("bad");

    Throwable exception = assertThrows(BadRequestResponse.class, () -> {
      huntController.getCompleteHunt(ctx);
    });

    assertEquals("The requested hunt id wasn't a legal Mongo Object ID.", exception.getMessage());
  }

  @Test
  void deleteTasksWithHunt() throws IOException {
    String testID = huntId.toHexString();
    when(ctx.pathParam("id")).thenReturn(testID);

    assertEquals(3, db.getCollection("tasks").countDocuments(eq("huntId", testID)));

    huntController.deleteTasks(ctx);

    assertEquals(0, db.getCollection("tasks").countDocuments(eq("huntId", testID)));
  }

@Test
  void startHuntCreatesNewStartedHunt() throws IOException {
    String testID = huntId.toHexString();
    when(ctx.pathParam("id")).thenReturn(testID);

    Document hunt = db.getCollection("hunts").find(eq("_id", new ObjectId(testID))).first();
    assertNotNull(hunt);

    huntController.startHunt(ctx);

    verify(ctx).status(HttpStatus.CREATED);

    Document startedHunt = db.getCollection("startedHunts").find(eq("completeHunt.hunt._id", new ObjectId(testID)))
        .first();
    assertNotNull(startedHunt);
    assertEquals(hunt.get("_id"),
        startedHunt.get("completeHunt", Document.class).get("hunt", Document.class).get("_id"));
    assertTrue(startedHunt.getBoolean("status"));
    assertNotNull(startedHunt.getString("accessCode"));
  }

  @Test
  void startHuntThrowsExceptionWhenHuntNotFound() throws IOException {
    String testID = "507f1f77bcf86cd799439011";
    when(ctx.pathParam("id")).thenReturn(testID);

    Document hunt = db.getCollection("hunts").find(eq("_id", new ObjectId(testID))).first();
    assertNull(hunt);

    Exception exception = assertThrows(NotFoundResponse.class, () -> {
      huntController.startHunt(ctx);
    });

    assertEquals("The requested hunt was not found", exception.getMessage());

    Document startedHunt = db.getCollection("startedHunts").find(eq("hunt._id", new ObjectId(testID))).first();
    assertNull(startedHunt);
  }
}
