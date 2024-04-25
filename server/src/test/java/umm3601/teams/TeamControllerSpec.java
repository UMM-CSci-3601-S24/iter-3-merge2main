package umm3601.teams;

import static com.mongodb.client.model.Filters.eq;
import static org.junit.jupiter.api.Assertions.assertEquals;
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
public class TeamControllerSpec {
  private TeamController teamController;
  private ObjectId teamId;

  private static MongoClient mongoClient;
  private static MongoDatabase db;
  private static JavalinJackson javalinJackson = new JavalinJackson();

  @Mock
  private Context ctx;

  @Captor
  private ArgumentCaptor<Team> teamCaptor;

  @Captor
  private ArgumentCaptor<ArrayList<Team>> teamArrayListCaptor;

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

    teamController = new TeamController(db);
  }

  @Test
  void testAddRoutes() {
    Javalin mockServer = mock(Javalin.class);
    teamController.addRoutes(mockServer);
    verify(mockServer, Mockito.atLeast(1)).get(any(), any());
  }

  @Test
  void testGetTeamById() throws IOException {
    String id = teamId.toHexString();
    when(ctx.pathParam("id")).thenReturn(id);

    teamController.getTeam(ctx);

    verify(ctx).json(teamCaptor.capture());
    verify(ctx).status(HttpStatus.OK);

    assertEquals("Team 4", teamCaptor.getValue().teamName, "Team name should be 'Team 4'");
  }

  @Test
  void testGetTeamWithBadId() throws IOException {
    when(ctx.pathParam("id")).thenReturn("bad_id");

    assertThrows(BadRequestResponse.class, () -> teamController.getTeam(ctx));
  }

  @Test
  void testGetTeamWithNonexistentId() throws IOException {
    when(ctx.pathParam("id")).thenReturn("000000000000000000000000");

    assertThrows(NotFoundResponse.class, () -> teamController.getTeam(ctx));
  }

  @Test
  void testCreateTeam() {
    // Mock the JSON payload
    String testNewTeam = """
        {
          "teamName": "Team 5",
          "startedHuntId": "startedHunt1"
        }
        """;

    // Mock the bodyValidator method to return the mock Team object
    when(ctx.bodyValidator(Team.class))
        .thenReturn(new BodyValidator<>(testNewTeam, Team.class, javalinJackson));

    // Call the createTeam method
    teamController.createTeam(ctx);

    // Verify that the response status is CREATED
    verify(ctx).status(HttpStatus.CREATED);

    // Verify that the JSON response contains the expected keys
    verify(ctx).json(mapCaptor.capture());
    Map<String, String> responseJson = mapCaptor.getValue();
    assertNotNull(responseJson);
    assertTrue(responseJson.containsKey("id"));
  }

  @Test
  void testCreateTeamWithNullName() {
    String testNewTeam = """
        {
          "teamName": null,
        }
        """;
    when(ctx.bodyValidator(Team.class))
        .then(value -> new BodyValidator<Team>(testNewTeam, Team.class, javalinJackson));

    assertThrows(ValidationException.class, () -> teamController.createTeam(ctx));
  }

  @Test
  void testCreateTeamWithEmptyName() {
    String testNewTeam = """
        {
          "teamName": "",
        }
        """;
    when(ctx.bodyValidator(Team.class))
        .then(value -> new BodyValidator<Team>(testNewTeam, Team.class, javalinJackson));

    assertThrows(ValidationException.class, () -> teamController.createTeam(ctx));
  }

  @Test
  void testGetTeams() throws IOException {
    teamController.getTeams(ctx);
    verify(ctx).json(any());
    verify(ctx).status(HttpStatus.OK);
  }

  @Test
  void testDeleteTeam() throws IOException {
    String id = teamId.toHexString();
    when(ctx.pathParam("id")).thenReturn(id);

    teamController.deleteTeam(ctx);
    verify(ctx).status(HttpStatus.NO_CONTENT);

    Document deletedTeam = db.getCollection("teams")
        .find(eq("_id", new ObjectId(id))).first();

    assertNull(deletedTeam);
  }

  @Test
  void testDeleteTeamWithBadId() throws IOException {
    when(ctx.pathParam("id")).thenReturn("badId");

    Throwable exception = assertThrows(BadRequestResponse.class, () -> {
      teamController.deleteTeam(ctx);
    });

    assertEquals("The requested team id wasn't a legal Mongo Object ID.", exception.getMessage());
  }

  @Test
  void testDeleteTeamWithNonExistentId() throws IOException {
    ObjectId id = new ObjectId();
    when(ctx.pathParam("id")).thenReturn(id.toHexString());

    Throwable exception = assertThrows(NotFoundResponse.class, () -> {
      teamController.deleteTeam(ctx);
    });

    assertEquals("The requested team was not found", exception.getMessage());
  }

  @Test
  void testCreateTeams() throws IOException {
    when(ctx.pathParam("startedHuntId")).thenReturn("startedHunt1");
    when(ctx.bodyAsClass(Integer.class)).thenReturn(2);

    teamController.createTeams(ctx);

    verify(ctx).status(HttpStatus.CREATED);
    verify(ctx).json(teamArrayListCaptor.capture());
    ArrayList<Team> teams = teamArrayListCaptor.getValue();
    assertEquals(2, teams.size());
    assertEquals("Team 1", teams.get(0).teamName);
    assertEquals("Team 2", teams.get(1).teamName);
  }

  @Test
  void testCreateTeamsWithTooManyTeams() throws IOException {
    when(ctx.pathParam("startedHuntId")).thenReturn("startedHunt1");
    when(ctx.bodyAsClass(Integer.class)).thenReturn(11);

    Throwable exception = assertThrows(BadRequestResponse.class, () -> {
      teamController.createTeams(ctx);
    });

    assertEquals("Invalid number of teams requested", exception.getMessage());
  }

  @Test
  void testCreateTeamsWithTooFewTeams() throws IOException {
    when(ctx.pathParam("startedHuntId")).thenReturn("startedHunt1");
    when(ctx.bodyAsClass(Integer.class)).thenReturn(0);

    Throwable exception = assertThrows(BadRequestResponse.class, () -> {
      teamController.createTeams(ctx);
    });

    assertEquals("Invalid number of teams requested", exception.getMessage());
  }

  @Test
  void testGetStartedHuntTeams() {
    when(ctx.pathParam("startedHuntId")).thenReturn("startedHunt1");

    teamController.getAllStartedHuntTeams(ctx);

    verify(ctx).json(any());
    verify(ctx).status(HttpStatus.OK);
  }

  @Test
  void testGetTeamsByStartedHuntId() {
    TeamController mockTeamController = new TeamController(db);
    String startedHuntId = "startedHunt1";

    ArrayList<Team> teams = mockTeamController.getTeamsByStartedHuntId(startedHuntId);

    assertNotNull(teams);
    assertEquals(2, teams.size());
    assertEquals("Team 1", teams.get(0).teamName);
    assertEquals("Team 4", teams.get(1).teamName);
  }
}
