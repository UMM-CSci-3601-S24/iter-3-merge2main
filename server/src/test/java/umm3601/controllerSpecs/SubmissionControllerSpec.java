package umm3601.controllerSpecs;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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
import org.mockito.MockedStatic;
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
import io.javalin.http.UploadedFile;
import umm3601.startedHunts.StartedHunt;
import umm3601.teams.Submission;
import umm3601.teams.SubmissionController;

public class SubmissionControllerSpec {
  private SubmissionController submissionController;
  private ObjectId startedHuntId;
  private ObjectId submissionId;
  private ObjectId newSubmissionId;
  private ObjectId teamId;
  private ObjectId taskId;
  private ObjectId huntId;

  private static MongoClient mongoClient;
  private static MongoDatabase db;

  @Mock
  private Context ctx;

  @Captor
  private ArgumentCaptor<Submission> submissionCaptor;

  @Captor
  private ArgumentCaptor<StartedHunt> startedHuntCaptor;

  @Captor
  private ArgumentCaptor<ArrayList<Submission>> submissionListCaptor;

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

    newSubmissionId = new ObjectId();
    Document newSubmission = new Document()
        .append("_id", newSubmissionId)
        .append("taskId", taskId.toHexString())
        .append("teamId", teamId.toHexString())
        .append("photoPath", "nonexistent.png") // This photo does not exist
        .append("submitTime", new Date());

    submissionDocuments.insertMany(testSubmissions);
    submissionDocuments.insertOne(submission);
    submissionDocuments.insertOne(newSubmission);

    MongoCollection<Document> startedHuntDocuments = db.getCollection("startedHunts");
    startedHuntDocuments.drop();
    List<Document> testStartedHunts = new ArrayList<>();
    ArrayList<String> submissionIds = new ArrayList<>();
    submissionIds.add(submissionId.toHexString());
    testStartedHunts.add(new Document()
        .append("accessCode", "Code 1")
        .append("completeHunt", new Document()
            .append("tasks", new ArrayList<Document>())
            .append("hunt", new Document()))
        .append("status", true)
        .append("endDate", new Date())
        .append("submissionIds", submissionIds));
    testStartedHunts.add(new Document()
        .append("accessCode", "Code 2")
        .append("completeHunt", new Document()
            .append("tasks", new ArrayList<Document>())
            .append("hunt", new Document()))
        .append("status", true)
        .append("endDate", new Date())
        .append("submissionIds", new ArrayList<String>()));
    testStartedHunts.add(new Document()
        .append("accessCode", "Code 3")
        .append("completeHunt", new Document()
            .append("tasks", new ArrayList<Document>())
            .append("hunt", new Document()))
        .append("status", true)
        .append("endDate", new Date())
        .append("submissionIds", new ArrayList<String>()));

    startedHuntId = new ObjectId();
    Document startedHunt = new Document()
        .append("_id", startedHuntId)
        .append("accessCode", "Code 4")
        .append("completeHunt", new Document()
            .append("tasks", new ArrayList<Document>())
            .append("hunt", new Document()))
        .append("status", true)
        .append("endDate", new Date())
        .append("submissionIds", submissionIds);

    startedHuntDocuments.insertMany(testStartedHunts);
    startedHuntDocuments.insertOne(startedHunt);

    MongoCollection<Document> teamDocuments = db.getCollection("teams");
    teamDocuments.drop();
    List<Document> testTeams = new ArrayList<>();
    testTeams.add(new Document()
        .append("teamName", "Team 1")
        .append("startedHuntId", "startedHunt1"));
    testTeams.add(new Document()
        .append("teamName", "Team 2")
        .append("startedHuntId", "startedHunt2"));

    teamId = new ObjectId();
    Document team = new Document()
        .append("_id", teamId)
        .append("teamName", "Team 4")
        .append("startedHuntId", "startedHunt1");

    teamDocuments.insertMany(testTeams);
    teamDocuments.insertOne(team);
    submissionController = new SubmissionController(db);
  }

  @BeforeEach
  void setup() throws IOException {
    MockitoAnnotations.openMocks(this);

    MongoCollection<Document> teamDocuments = db.getCollection("teams");
    teamDocuments.drop();
    List<Document> testTeams = new ArrayList<>();
    testTeams.add(new Document()
        .append("teamName", "Team 1")
        .append("startedHuntId", "startedHunt1"));
    testTeams.add(new Document()
        .append("teamName", "Team 2")
        .append("startedHuntId", "startedHunt2"));
    testTeams.add(new Document()
        .append("teamName", "Team 3")
        .append("startedHuntId", "startedHunt3"));

    teamId = new ObjectId();
    Document team = new Document()
        .append("_id", teamId)
        .append("teamName", "Team 4")
        .append("startedHuntId", "startedHunt1");

    teamDocuments.insertMany(testTeams);
    teamDocuments.insertOne(team);
    MongoCollection<Document> taskDocuments = db.getCollection("tasks");
    taskDocuments.drop();
    huntId = new ObjectId();
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
    submissionController.addRoutes(mockServer);
    verify(mockServer, Mockito.atLeast(1)).get(any(), any());
  }

  @Test
  public void testCreateSubmission() {
    SubmissionController mockSubmissionController = new SubmissionController(db);

    String taskId = "task1";
    String teamId = "team1";
    String photoPath = "/path/to/photo.jpg";

    Submission submission = mockSubmissionController.createSubmission(taskId, teamId, photoPath);

    assertNotNull(submission);
    assertEquals(taskId, submission.taskId);
    assertEquals(teamId, submission.teamId);
    assertEquals(photoPath, submission.photoPath);
    assertNotNull(submission.submitTime);
  }

  @Test
  void testGetSubmission() {
    when(ctx.pathParam("id")).thenReturn(submissionId.toHexString());

    submissionController.getSubmission(ctx);

    verify(ctx).json(submissionCaptor.capture());
    verify(ctx).status(HttpStatus.OK);

    assertEquals(submissionId.toHexString(), submissionCaptor.getValue()._id);
  }

  @Test
  public void testGetSubmissionsByTeam() {
    when(ctx.pathParam("teamId")).thenReturn("Team 1");
    submissionController.getSubmissionsByTeam(ctx);

    verify(ctx).json(submissionListCaptor.capture());
    verify(ctx).status(HttpStatus.OK);

    List<Submission> submissions = submissionListCaptor.getValue();
    assertFalse(submissions.isEmpty());
  }

  @Test
  void testGetSubmissionsByTask() {
    when(ctx.pathParam("taskId")).thenReturn("Task 1");
    submissionController.getSubmissionsByTask(ctx);

    verify(ctx).json(submissionListCaptor.capture());
    verify(ctx).status(HttpStatus.OK);

    List<Submission> submissions = submissionListCaptor.getValue();
    assertFalse(submissions.isEmpty());
  }

  @Test
  void testGetSubmissionsByTaskNoSubmissions() {
    when(ctx.pathParam("taskId")).thenReturn("Task 5");
    submissionController.getSubmissionsByTask(ctx);

    verify(ctx).json(submissionListCaptor.capture());
    verify(ctx).status(HttpStatus.OK);

    List<Submission> submissions = submissionListCaptor.getValue();
    assertTrue(submissions.isEmpty());
  }

  @Test
  void testGetSubmissionsByTeamNoSubmissions() {
    when(ctx.pathParam("teamId")).thenReturn("Team 5");
    submissionController.getSubmissionsByTeam(ctx);

    verify(ctx).json(submissionListCaptor.capture());
    verify(ctx).status(HttpStatus.OK);

    List<Submission> submissions = submissionListCaptor.getValue();
    assertTrue(submissions.isEmpty());
  }

  @Test
  void testGetSubmissionByTeamAndTask() {
    when(ctx.pathParam("teamId")).thenReturn("Team 1");
    when(ctx.pathParam("taskId")).thenReturn("Task 1");
    submissionController.getSubmissionByTeamAndTask(ctx);

    verify(ctx).json(submissionCaptor.capture());
    verify(ctx).status(HttpStatus.OK);

    Submission submission = submissionCaptor.getValue();
    assertNotNull(submission);
    assertEquals("Team 1", submission.teamId);
    assertEquals("Task 1", submission.taskId);
  }

  @Test
  void testGetSubmissionsByStartedHunt() {
    when(ctx.pathParam("startedHuntId")).thenReturn(startedHuntId.toHexString());
    submissionController.getSubmissionsByStartedHunt(ctx);

    verify(ctx).json(submissionListCaptor.capture());
    verify(ctx).status(HttpStatus.OK);

    List<Submission> submissions = submissionListCaptor.getValue();
    assertFalse(submissions.isEmpty());
  }

  @Test
  void testGetSubmissionsByStartedHuntNoSubmissions() {
    when(ctx.pathParam("startedHuntId")).thenReturn(new ObjectId().toHexString());
    submissionController.getSubmissionsByStartedHunt(ctx);

    verify(ctx).json(submissionListCaptor.capture());
    verify(ctx).status(HttpStatus.OK);

    List<Submission> submissions = submissionListCaptor.getValue();
    assertTrue(submissions.isEmpty());
  }

  @Test
  void testGetSubmissionsByStartedHuntInvalidId() {
    when(ctx.pathParam("startedHuntId")).thenReturn("invalidId");

    assertThrows(IllegalArgumentException.class, () -> {
      submissionController.getSubmissionsByStartedHunt(ctx);
    });
  }

  @Test
  void testGetPhotoFromSubmission() {
    when(ctx.pathParam("id")).thenReturn(submissionId.toHexString());

    // Mock the photo file
    File photo = mock(File.class);
    when(photo.exists()).thenReturn(true);

    submissionController.getPhotoFromSubmission(ctx);

    verify(ctx).status(HttpStatus.OK);
  }

  @Test
  void testGetPhotoFromSubmissionWithNonexistentPhoto() {
    // Use the new submissionId for the test
    when(ctx.pathParam("id")).thenReturn(newSubmissionId.toHexString());

    // Mock the photo file
    File photo = mock(File.class);
    when(photo.exists()).thenReturn(false);

    submissionController.getPhotoFromSubmission(ctx);

    verify(ctx).result("");
    verify(ctx).status(HttpStatus.NOT_FOUND);
  }

  @Test
  void testGetSubmissionByTeamAndTaskNoSubmissionFound() {
    when(ctx.pathParam("teamId")).thenReturn("NonExistentTeam");
    when(ctx.pathParam("taskId")).thenReturn("NonExistentTask");

    submissionController.getSubmissionByTeamAndTask(ctx);

    verify(ctx).status(HttpStatus.NOT_FOUND);

    verify(ctx, Mockito.never()).json(any());
  }

  // @Test
  // void testDeleteSubmission() {
  // when(ctx.pathParam("id")).thenReturn(submissionId.toHexString());
  // when(ctx.pathParam("photoPath")).thenReturn("test.png");

  // submissionController.deleteSubmission(ctx, submissionId.toHexString());

  // verify(ctx).status(HttpStatus.NO_CONTENT);
  // // save new test.png file to photos directory
  // }

  @Test
  void testDeleteSubmissionWithNonexistentPhoto() {
    when(ctx.pathParam("id")).thenReturn(newSubmissionId.toHexString());
    when(ctx.pathParam("photoPath")).thenReturn("nonexistent.png");

    submissionController.deleteSubmission(ctx, newSubmissionId.toHexString());

    verify(ctx).status(HttpStatus.NO_CONTENT);
  }

  @Test
  void testDeleteSubmissionWithNonexistentSubmission() {
    when(ctx.pathParam("id")).thenReturn(new ObjectId().toHexString());

    assertThrows(NotFoundResponse.class, () -> {
      submissionController.deleteSubmission(ctx, new ObjectId().toHexString());
    });
  }

  @Test
  void testDeleteSubmissions() {
    ArrayList<String> submissionIdsList = new ArrayList<>();
    submissionIdsList.add(submissionId.toHexString());

    submissionController.deleteSubmissions(submissionIdsList);

    // Verify that the submission the submission was deleted from database
    assertEquals(0, db.getCollection("submissions").countDocuments(new Document("_id", submissionId)));

  }

  @Test
  void testGetFileExtension() {
    SubmissionController testSubmissionController = new SubmissionController(db);

    String filename1 = "test.jpg";
    String filename2 = "document.pdf";
    String filename3 = "file_without_extension";

    assertEquals("jpg", testSubmissionController.getFileExtension(filename1));
    assertEquals("pdf", testSubmissionController.getFileExtension(filename2));
    assertEquals("", testSubmissionController.getFileExtension(filename3));
  }

  @Test
  public void testUploadPhotoSuccess() throws IOException {
    // Mock the uploaded file
    UploadedFile uploadedFile = Mockito.mock(UploadedFile.class);
    when(uploadedFile.content()).thenReturn(new ByteArrayInputStream("test photo content".getBytes()));
    when(uploadedFile.filename()).thenReturn("test.jpg");

    // Mock the context
    when(ctx.uploadedFile("photo")).thenReturn(uploadedFile);

    // Call the method under test
    String result = submissionController.uploadPhoto(ctx);

    // Verify the behavior and assertions
    assertNotNull(result);
    assertTrue(result.matches("[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}\\.jpg"));

    // Verify that the file was saved
    String id = result.substring(0, result.lastIndexOf('.'));
    String extension = result.substring(result.lastIndexOf('.') + 1);
    File savedFile = new File("photos", id + "." + extension);
    assertTrue(savedFile.exists());
    assertEquals("test photo content", Files.readString(savedFile.toPath()));

    // Verify the context status
    verify(ctx).status(HttpStatus.OK);
  }

  @Test
  public void testUploadPhotoNoFileUploaded() {
    // Mock the context
    when(ctx.uploadedFile("photo")).thenReturn(null);

    // Call the method under test and assert the exception
    BadRequestResponse exception = assertThrows(BadRequestResponse.class, () -> submissionController.uploadPhoto(ctx));
    assertEquals("No photo uploaded", exception.getMessage());
  }

  // @SuppressWarnings("static-access")
  // @Test
  // public void testUploadPhotoErrorCopyingFile() throws IOException {
  // // Mock the uploaded file
  // UploadedFile uploadedFile = Mockito.mock(UploadedFile.class);
  // when(uploadedFile.content()).thenReturn(new ByteArrayInputStream("test photo
  // content".getBytes()));
  // when(uploadedFile.filename()).thenReturn("test.jpg");

  // // Mock the context
  // when(ctx.uploadedFile("photo")).thenReturn(uploadedFile);

  // // Mock the file copy to throw an IOException
  // Files filesMock = Mockito.mock(Files.class);
  // doThrow(new IOException("File copy
  // error")).when(filesMock).copy(any(InputStream.class), any(Path.class),
  // any());

  // // Call the method under test and assert the exception
  // IOException exception = assertThrows(IOException.class, () ->
  // submissionController.uploadPhoto(ctx));
  // assertEquals("Error handling the uploaded file: File copy error",
  // exception.getMessage());
  // }

  // @Test
  // public void testUploadPhotoUnexpectedError() {
  // // Mock the context to throw an exception
  // when(ctx.uploadedFile("photo")).thenThrow(new RuntimeException("Unexpected
  // error"));

  // // Call the method under test and assert the exception
  // BadRequestResponse exception = assertThrows(BadRequestResponse.class, () ->
  // submissionController.uploadPhoto(ctx));
  // assertEquals("Unexpected error during photo upload: Unexpected error",
  // exception.getMessage());
  // }

  @Test
  void testAddPhotoPathToSubmission() throws IOException {
    String photoPath = "test.jpg";

    // Set up the same taskId and teamId used in the test setup
    String taskId = "Task 4";
    String teamId = "Team 4";

    // Delete any existing submission with the same taskId and teamId
    db.getCollection("submissions").deleteMany(and(eq("taskId", taskId), eq("teamId", teamId)));

    // Mock the context
    when(ctx.pathParam("taskId")).thenReturn(taskId);
    when(ctx.pathParam("teamId")).thenReturn(teamId);
    when(ctx.pathParam("startedHuntId")).thenReturn(startedHuntId.toHexString());

    submissionController.addPhotoPathToSubmission(ctx, photoPath);

    Document updatedSubmission = db.getCollection("submissions")
        .find(and(eq("taskId", taskId), eq("teamId", teamId))).first();

    System.out.println("Updated submission: " + updatedSubmission.toJson());

    assertNotNull(updatedSubmission);
    assertEquals(photoPath, updatedSubmission.get("photoPath"));
  }

  @Test
  void testUpdatePhotoPathInExistingSubmission() throws IOException {
    String photoPath = "test.jpg";

    // Set up the same taskId and teamId used in the test setup
    String taskId = "Task 4";
    String teamId = "Team 4";

    // Mock the context
    when(ctx.pathParam("taskId")).thenReturn(taskId);
    when(ctx.pathParam("teamId")).thenReturn(teamId);
    when(ctx.pathParam("startedHuntId")).thenReturn(startedHuntId.toHexString());

    // Ensure a submission already exists with the same taskId and teamId
    Document existingSubmission = db.getCollection("submissions")
        .find(and(eq("taskId", taskId), eq("teamId", teamId))).first();

    System.out.println("Existing submission before update: " + existingSubmission);

    if (existingSubmission == null) {
      // If no existing submission, create a new one
      Document newSubmission = new Document("taskId", taskId)
          .append("teamId", teamId)
          .append("photoPath", "oldTest.jpg");
      db.getCollection("submissions").insertOne(newSubmission);
      System.out.println("New submission created: " + newSubmission);
    }

    submissionController.addPhotoPathToSubmission(ctx, photoPath);

    Document updatedSubmission = db.getCollection("submissions")
        .find(and(eq("taskId", taskId), eq("teamId", teamId))).first();

    System.out.println("Updated submission: " + updatedSubmission);

    assertNotNull(updatedSubmission);
    assertEquals(photoPath, updatedSubmission.get("photoPath"));
  }

  @Test
  void testReplacePhotoWithContext() throws IOException {
    SubmissionController submissionController = Mockito.mock(SubmissionController.class);

    String photoPath = "test.png";

    // Set up the same taskId and teamId used in the test setup
    String taskId = "Task 4";
    String teamId = "Team 4";

    // Mock the context
    when(ctx.pathParam("taskId")).thenReturn(taskId);
    when(ctx.pathParam("teamId")).thenReturn(teamId);
    when(ctx.pathParam("startedHuntId")).thenReturn(startedHuntId.toHexString());

    // Ensure a submission already exists with the same taskId and teamId
    Document existingSubmission = db.getCollection("submissions")
        .find(and(eq("taskId", taskId), eq("teamId", teamId))).first();

    System.out.println("Existing submission before update: " + existingSubmission);

    if (existingSubmission == null) {
      // If no existing submission, create a new one
      Document newSubmission = new Document("taskId", taskId)
          .append("teamId", teamId)
          .append("photoPath", "oldTest.jpg");
      db.getCollection("submissions").insertOne(newSubmission);
      System.out.println("New submission created: " + newSubmission);
    }

    // Mock the file deletion operation
    // Simulate the deletion by not performing any action
    doNothing().when(submissionController).deletePhoto(anyString(), any());

    submissionController.replacePhoto(ctx);

    Document updatedSubmission = db.getCollection("submissions")
        .find(and(eq("taskId", taskId), eq("teamId", teamId))).first();

    System.out.println("Updated submission: " + updatedSubmission);

    assertNotNull(updatedSubmission);
    assertEquals(photoPath, updatedSubmission.get("photoPath"));
  }


    @Test
    public void testDeletePhoto() throws IOException {
        // Create a mock Context object
        Context ctx = mock(Context.class);

        // Create a mock Path object
        Path filePath = mock(Path.class);

        // Mock the behavior of Files.exists() method
        when(Files.exists(filePath)).thenReturn(true);

        // Mock the behavior of Files.delete() method
        MockedStatic<Files> mockedFiles = Mockito.mockStatic(Files.class);
        mockedFiles.when(() -> Files.delete(filePath)).thenReturn(null);

        // Create an instance of SubmissionController
        SubmissionController submissionController = new SubmissionController(db);

        // Call the deletePhoto method
        submissionController.deletePhoto("photoId", ctx);

        // Verify that the status is set to HttpStatus.OK
        verify(ctx).status(HttpStatus.OK);

        // Verify that Files.delete() method is called with the correct filePath
        mockedFiles.verify(() -> Files.delete(filePath));
    }
}


