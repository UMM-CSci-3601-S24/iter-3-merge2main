package umm3601.host;

import static com.mongodb.client.model.Filters.eq;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
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

@SuppressWarnings({ "MagicNumber" })
class StartedHuntControllerSpec {
  private StartedHuntController startedHuntController;
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

    MongoCollection<Document> startedHuntsDocuments = db.getCollection("startedHunts");
    startedHuntsDocuments.drop();
    List<Document> startedHunts = new ArrayList<>();
    Calendar calendar = Calendar.getInstance();
    calendar.set(2024, Calendar.MAY, 2, 12, 0, 0);
    Date date = calendar.getTime();
    startedHunts.add(
        new Document()
            .append("accessCode", "123456")
            .append("completeHunt", new Document()
                .append("hunt", testHunts.get(0))
                .append("tasks", testTasks.subList(0, 2)))
            .append("status", true)
            .append("endDate", null));

    startedHunts.add(
        new Document()
            .append("accessCode", "654321")
            .append("completeHunt", new Document()
                .append("hunt", testHunts.get(1))
                .append("tasks", testTasks.subList(2, 3)))
            .append("status", false)
            .append("endDate", date));

    startedHunts.add(
        new Document()
            .append("accessCode", "123459")
            .append("completeHunt", new Document()
                .append("hunt", testHunts.get(2))
                .append("tasks", testTasks.subList(0, 3)))
            .append("status", true)
            .append("endDate", null));

    startedHuntId = new ObjectId();
    Document startedHunt = new Document()
        .append("_id", startedHuntId)
        .append("accessCode", "123456")
        .append("completeHunt", new Document()
            .append("hunt", testHunts.get(2))
            .append("tasks", testTasks.subList(0, 3)))
        .append("status", true)
        .append("endDate", null);

    startedHuntsDocuments.insertMany(startedHunts);
    startedHuntsDocuments.insertOne(startedHunt);

    startedHuntController = new StartedHuntController(db);
  }

  @Test
  void addRoutes() {
    Javalin mockServer = mock(Javalin.class);
    startedHuntController.addRoutes(mockServer);
    verify(mockServer, Mockito.atLeast(1)).get(any(), any());
  }

  @Test
  void startHuntCreatesNewStartedHunt() throws IOException {
    String testID = huntId.toHexString();
    when(ctx.pathParam("id")).thenReturn(testID);

    Document hunt = db.getCollection("hunts").find(eq("_id", new ObjectId(testID))).first();
    assertNotNull(hunt);

    startedHuntController.startHunt(ctx);

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
      startedHuntController.startHunt(ctx);
    });

    assertEquals("The requested hunt was not found", exception.getMessage());

    Document startedHunt = db.getCollection("startedHunts").find(eq("hunt._id", new ObjectId(testID))).first();
    assertNull(startedHunt);
  }

  @Test
  void getStartedHunt() throws IOException {
    when(ctx.pathParam("accessCode")).thenReturn("123456");

    startedHuntController.getStartedHunt(ctx);

    verify(ctx).json(startedHuntCaptor.capture());
    verify(ctx).status(HttpStatus.OK);

    assertEquals("123456", startedHuntCaptor.getValue().accessCode);
    assertEquals(true, startedHuntCaptor.getValue().status);
  }

  @Test
  void getStartedHuntWithNonExistentAccessCode() throws IOException {
    when(ctx.pathParam("accessCode")).thenReturn("588935");

    Throwable exception = assertThrows(NotFoundResponse.class, () -> {
      startedHuntController.getStartedHunt(ctx);
    });

    assertEquals("The requested access code was not found.", exception.getMessage());
  }

  @Test
  void getStartedHuntWithInvalidAccessCode() throws IOException {
    when(ctx.pathParam("accessCode")).thenReturn("12345"); // 5-digit number

    Throwable exception = assertThrows(BadRequestResponse.class, () -> {
      startedHuntController.getStartedHunt(ctx);
    });

    assertEquals("The requested access code is not a valid access code.", exception.getMessage());
  }

  @Test
  void getStartedHuntWithNonNumericAccessCode() throws IOException {
    when(ctx.pathParam("accessCode")).thenReturn("123abc"); // Access code with non-numeric characters

    Throwable exception = assertThrows(BadRequestResponse.class, () -> {
      startedHuntController.getStartedHunt(ctx);
    });

    assertEquals("The requested access code is not a valid access code.", exception.getMessage());
  }

  @Test
  void getStartedHuntWithStatusFalse() throws IOException {
    when(ctx.pathParam("accessCode")).thenReturn("654321");

    Throwable exception = assertThrows(BadRequestResponse.class, () -> {
      startedHuntController.getStartedHunt(ctx);
    });

    assertEquals("The requested hunt is no longer joinable.", exception.getMessage());
  }
  @Test
  void getEndedHunts() throws IOException {
    startedHuntController.getEndedHunts(ctx);

    verify(ctx).json(startedHuntArrayListCaptor.capture());

    assertEquals(1, startedHuntArrayListCaptor.getValue().size());
    for (StartedHunt startedHunt : startedHuntArrayListCaptor.getValue()) {
      assertEquals(false, startedHunt.status);
    }
  }

  @Test
  void endStartedHunt() throws IOException {
    when(ctx.pathParam("id")).thenReturn(startedHuntId.toHexString());
    when(ctx.pathParam("accessCode")).thenReturn("123456");

    // Check the initial status
    startedHuntController.getStartedHunt(ctx);
    verify(ctx).json(startedHuntCaptor.capture());
    assertEquals(true, startedHuntCaptor.getValue().status);
    assertNull(startedHuntCaptor.getValue().endDate); // Check that the endDate is null

    // End the hunt
    startedHuntController.endStartedHunt(ctx);
    verify(ctx, times(2)).status(HttpStatus.OK);

    // Check the status and endDate after ending the hunt
    startedHuntController.getEndedHunts(ctx);
    verify(ctx).json(startedHuntArrayListCaptor.capture());
    for (StartedHunt startedHunt : startedHuntArrayListCaptor.getValue()) {
      if (startedHunt._id.equals("123456")) {
        assertEquals(false, startedHunt.status);
      }
    }
  }

  @Test
  void endStartedHuntIsNull() throws IOException {
    when(ctx.pathParam("id")).thenReturn("588935f57546a2daea54de8c");

    assertThrows(NotFoundResponse.class, () -> {
      startedHuntController.endStartedHunt(ctx);
    });
  }

  @Test
  void tryToDeleteNotFoundStartedHunt() throws IOException {
    String testID = startedHuntId.toHexString();
    when(ctx.pathParam("id")).thenReturn(testID);

    startedHuntController.deleteStartedHunt(ctx);
    assertEquals(0, db.getCollection("hunts").countDocuments(eq("_id", new ObjectId(testID))));

    assertThrows(NotFoundResponse.class, () -> {
      startedHuntController.deleteStartedHunt(ctx);
    });

    verify(ctx).status(HttpStatus.NOT_FOUND);
    assertEquals(0, db.getCollection("hunts").countDocuments(eq("_id", new ObjectId(testID))));
  }


}
