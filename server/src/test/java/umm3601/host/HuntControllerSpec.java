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
import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import io.javalin.http.NotFoundResponse;
import io.javalin.json.JavalinJackson;
import io.javalin.validation.BodyValidator;
import io.javalin.validation.ValidationException;

@SuppressWarnings({ "MagicNumber" })
class HuntControllerSpec {
  private HuntController huntController;
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
  }

  @Test
  void addRoutes() {
    Javalin mockServer = mock(Javalin.class);
    huntController.addRoutes(mockServer);
    verify(mockServer, Mockito.atLeast(1)).get(any(), any());
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
}
