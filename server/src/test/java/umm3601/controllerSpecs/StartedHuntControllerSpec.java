package umm3601.controllerSpecs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static com.mongodb.client.model.Filters.eq;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

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
import umm3601.startedHunts.StartedHunt;
import umm3601.startedHunts.StartedHuntController;

@SuppressWarnings({ "MagicNumber" })
public class StartedHuntControllerSpec {
  private StartedHuntController startedHuntController;
  private ObjectId startedHuntId;
  private ObjectId submissionId;
  private ObjectId frysId;
  private ObjectId huntId;
  private ObjectId taskId;
  private ObjectId teamId;

  private static MongoClient mongoClient;
  private static MongoDatabase db;

  @Mock
  private Context ctx;

  @Captor
  ArgumentCaptor<Document> startedHuntDocumentCaptor;

  @Captor
  ArgumentCaptor<StartedHunt> startedHuntCaptor;

  @Captor
  ArgumentCaptor<ArrayList<StartedHunt>> startedHuntArrayListCaptor;

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
            .append("hostId", frysId.toHexString())
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
        .append("hostId", frysId.toHexString())
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

    MongoCollection<Document> submissionDocuments = db.getCollection("submissions");
    submissionDocuments.drop();
    List<Document> testSubmissions = new ArrayList<>();
    testSubmissions.add(new Document()
        .append("taskId", "Task 1")
        .append("teamId", "Team 1")
        .append("photoPath", "Path 1")
        .append("submitTime", new Date()));
    testSubmissions.add(new Document()
        .append("taskId", "Task 2")
        .append("teamId", "Team 2")
        .append("photoPath", "Path 2")
        .append("submitTime", new Date()));
    testSubmissions.add(new Document()
        .append("taskId", "Task 3")
        .append("teamId", "Team 3")
        .append("photoPath", "Path 3")
        .append("submitTime", new Date()));

    submissionId = new ObjectId();
    Document submission = new Document()
        .append("_id", submissionId)
        .append("taskId", "Task 4")
        .append("teamId", "Team 4")
        .append("photoPath", "test.png")
        .append("submitTime", new Date());

    submissionDocuments.insertMany(testSubmissions);
    submissionDocuments.insertOne(submission);

    MongoCollection<Document> startedHuntDocuments = db.getCollection("startedHunts");
    startedHuntDocuments.drop();
    List<Document> testStartedHunts = new ArrayList<>();
    ArrayList<String> submissionIds = new ArrayList<>();
    submissionId = new ObjectId();
    submissionIds.add(submissionId.toHexString());
    testStartedHunts.add(
        new Document()
            .append("accessCode", "123456")
            .append("completeHunt", new Document()
                .append("tasks", new ArrayList<Document>())
                .append("hunt", hunt))
            .append("status", true)
            .append("endDate", null)
            .append("submissionIds", submissionIds));
    testStartedHunts.add(
        new Document()
            .append("accessCode", "654321")
            .append("completeHunt", new Document()
                .append("tasks", new ArrayList<Document>())
                .append("hunt", new Document()))
            .append("status", false)
            .append("endDate", new Date())
            .append("submissionIds", new ArrayList<String>()));
    testStartedHunts.add(
        new Document()
            .append("accessCode", "121212")
            .append("completeHunt", new Document()
                .append("tasks", new ArrayList<Document>())
                .append("hunt", new Document()))
            .append("status", true)
            .append("endDate", new Date())
            .append("submissionIds", new ArrayList<String>()));

    startedHuntId = new ObjectId();
    Document startedHunt = new Document()
        .append("_id", startedHuntId)
        .append("accessCode", "232323")
        .append("completeHunt", new Document()
            .append("tasks", new ArrayList<Document>())
            .append("hunt", hunt))
        .append("status", true)
        .append("endDate", null)
        .append("submissionIds", submissionIds);

    startedHuntDocuments.insertMany(testStartedHunts);
    startedHuntDocuments.insertOne(startedHunt);

    MongoCollection<Document> teamDocuments = db.getCollection("teams");
    teamDocuments.drop();
    List<Document> testTeams = new ArrayList<>();
    testTeams.add(new Document()
        .append("teamName", "Team 1")
        .append("startedHuntId", startedHuntId.toHexString()));
    testTeams.add(new Document()
        .append("teamName", "Team 2")
        .append("startedHuntId", startedHuntId.toHexString()));
    testTeams.add(new Document()
        .append("teamName", "Team 3")
        .append("startedHuntId", startedHuntId.toHexString()));

    teamId = new ObjectId();
    Document team = new Document()
        .append("_id", teamId)
        .append("teamName", "Team 4")
        .append("startedHuntId", "startedHunt1");

    teamDocuments.insertMany(testTeams);
    teamDocuments.insertOne(team);
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
  void deleteStartedHunt() throws IOException {
    when(ctx.pathParam("id")).thenReturn(startedHuntId.toHexString());

    // Get the StartedHunt before deletion to retrieve its submissionIds
    StartedHunt startedHuntBeforeDeletion = startedHuntController.getStartedHuntById(ctx);
    List<String> submissionIds = startedHuntBeforeDeletion.getSubmissionIds();

    startedHuntController.deleteStartedHunt(ctx);

    verify(ctx).status(HttpStatus.NO_CONTENT);

    Document startedHunt = db.getCollection("startedHunts").find(eq("_id", startedHuntId)).first();
    assertNull(startedHunt);

    Document team = db.getCollection("teams").find(eq("startedHuntId", startedHuntId)).first();
    assertNull(team);

    // Check that each submission referenced by the submissionIds no longer exists
    for (String submissionId : submissionIds) {
      Document submission = db.getCollection("submissions").find(eq("_id", new ObjectId(submissionId))).first();
      assertNull(submission);
    }
  }

  @Test
  void deleteStartedHuntIsNull() throws IOException {
    when(ctx.pathParam("id")).thenReturn("588935f57546a2daea54de8c");

    assertThrows(NotFoundResponse.class, () -> {
      startedHuntController.deleteStartedHunt(ctx);
    });
  }

  @Test
  void testGetStartedHuntById() throws IOException {
    when(ctx.pathParam("id")).thenReturn(startedHuntId.toHexString());

    startedHuntController.getStartedHuntById(ctx);

    verify(ctx).json(startedHuntCaptor.capture());
    assertEquals("232323", startedHuntCaptor.getValue().accessCode);
    assertEquals(true, startedHuntCaptor.getValue().status);
  }

  @Test
  void testGetStartedHuntByIdNotFound() throws IOException {
    when(ctx.pathParam("id")).thenReturn("588935f57546a2daea54de8c");

    assertThrows(NotFoundResponse.class, () -> {
      startedHuntController.getStartedHuntById(ctx);
    });
  }

  @Test
  void testGetStartedHuntByIdWithInvalidId() throws IOException {
    when(ctx.pathParam("id")).thenReturn("12345"); // 5-digit number

    Throwable exception = assertThrows(BadRequestResponse.class, () -> {
      startedHuntController.getStartedHuntById(ctx);
    });

    assertEquals("The requested started hunt id wasn't a legal Mongo Object ID.", exception.getMessage());
  }

  @Test
  void testGetStartedHuntsByHostId() throws IOException {
    when(ctx.pathParam("hostId")).thenReturn(frysId.toHexString());

    startedHuntController.getStartedHuntsByHostId(ctx);

    verify(ctx).json(startedHuntArrayListCaptor.capture());
    assertEquals(2, startedHuntArrayListCaptor.getValue().size());

    List<String> expectedAccessCodes = Arrays.asList("123456", "232323");
    for (StartedHunt startedHunt : startedHuntArrayListCaptor.getValue()) {
      assertTrue(expectedAccessCodes.contains(startedHunt.accessCode));
      assertEquals(true, startedHunt.status);
    }
  }

  @Test
  public void testDeleteStartedHuntDeletesSubmission() {
    when(ctx.pathParam("id")).thenReturn(startedHuntId.toHexString());

    // Get the StartedHunt before deletion to retrieve its submissionIds
    StartedHunt startedHuntBeforeDeletion = startedHuntController.getStartedHuntById(ctx);
    List<String> submissionIds = startedHuntBeforeDeletion.getSubmissionIds();

    startedHuntController.deleteStartedHunt(ctx);

    verify(ctx).status(HttpStatus.NO_CONTENT);

    Document startedHunt = db.getCollection("startedHunts").find(eq("_id", startedHuntId)).first();
    assertNull(startedHunt);

    Document team = db.getCollection("teams").find(eq("startedHuntId", startedHuntId)).first();
    assertNull(team);

    // Check that each submission referenced by the submissionIds no longer exists
    for (String submissionId : submissionIds) {
      Document submission = db.getCollection("submissions").find(eq("_id", new ObjectId(submissionId))).first();
      assertNull(submission);
    }
  }
}
