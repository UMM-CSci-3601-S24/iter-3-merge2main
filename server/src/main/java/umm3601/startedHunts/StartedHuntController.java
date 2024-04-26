package umm3601.startedHunts;

import static com.mongodb.client.model.Filters.eq;

import java.util.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bson.UuidRepresentation;
import org.bson.types.ObjectId;
import org.mongojack.JacksonMongoCollection;

import com.mongodb.client.MongoDatabase;

import io.javalin.Javalin;
import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import io.javalin.http.NotFoundResponse;
import umm3601.Controller;
import umm3601.host.CompleteHunt;
import umm3601.host.HostController;
import umm3601.teams.Submission;
import umm3601.teams.SubmissionController;
import umm3601.teams.Team;
import umm3601.teams.TeamController;

public class StartedHuntController implements Controller {

  private static final String API_START_HUNT = "/api/startHunt/{id}";
  private static final String API_STARTED_HUNT = "/api/startedHunts/{accessCode}";
  private static final String API_END_HUNT = "/api/endHunt/{id}";
  private static final String API_STARTED_HUNTS = "/api/startedHunts/host/{hostId}";
  private static final String API_ENDED_HUNTS = "/api/endedHunts";
  private static final String API_DELETE_HUNT = "/api/startedHunts/{id}";

  static final String HOST_KEY = "hostId";
  static final String STARTEDHUNT_HOST_KEY = "completeHunt.hunt.hostId";

  private static final int ACCESS_CODE_MIN = 100000;
  private static final int ACCESS_CODE_RANGE = 900000;
  private static final int ACCESS_CODE_LENGTH = 6;

  private final JacksonMongoCollection<StartedHunt> startedHuntCollection;

  private SubmissionController submissionController;
  private TeamController teamController;
  private HostController hostController;

  public StartedHuntController(MongoDatabase database) {

    startedHuntCollection = JacksonMongoCollection.builder().build(
        database,
        "startedHunts",
        StartedHunt.class,
        UuidRepresentation.STANDARD);

    submissionController = new SubmissionController(database);
    teamController = new TeamController(database);
    hostController = new HostController(database);

  }

  public void startHunt(Context ctx) {
    CompleteHunt completeHunt = new CompleteHunt();
    completeHunt.hunt = hostController.getHunt(ctx);
    completeHunt.tasks = hostController.getTasks(ctx);

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

  /**
   * Retrieves a StartedHunt using the access code from the Javalin Context.
   * Validates the access code and checks if the hunt is joinable.
   * Throws exceptions for invalid access codes, non-existent hunts, or
   * non-joinable hunts.
   * If a valid, joinable hunt is found, it is returned and its details are sent
   * as a JSON response.
   *
   * @param ctx a Javalin Context object with the HTTP request information.
   * @return a StartedHunt object if a valid, joinable hunt is found; otherwise,
   *         an exception is thrown.
   */
  public StartedHunt getStartedHunt(Context ctx) {
    String accessCode = ctx.pathParam("accessCode");
    StartedHunt startedHunt;

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

  /**
   * Gets a StartedHunt by its ID from the Javalin Context.
   *
   * @param ctx a Javalin Context object with the HTTP request information.
   * @return a StartedHunt object if the hunt is found; otherwise, a
   *         NotFoundResponse is thrown.
   */
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
      ctx.json(startedHunt);
      ctx.status(HttpStatus.OK);
      return startedHunt;
    }
  }

  /**
   * Retrieves all started hunts from the startedHuntCollection.
   * The list of started hunts is sent as a JSON response.
   *
   * @param ctx a Javalin Context object with the HTTP request information.
   */
  public void getStartedHuntsByHostId(Context ctx) {
    String hostId = ctx.pathParam(HOST_KEY);
    List<StartedHunt> startedHunts = startedHuntCollection.find(eq(STARTEDHUNT_HOST_KEY, hostId))
        .into(new ArrayList<>());
    ctx.json(startedHunts);
    ctx.status(HttpStatus.OK);
  }

  /**
   * Retrieves all ended hunts from the startedHuntCollection.
   * Hunts are considered ended if their status is false.
   * The list of ended hunts is sent as a JSON response.
   *
   * @param ctx a Javalin Context object with the HTTP request information.
   */
  public void getEndedHunts(Context ctx) {
    List<StartedHunt> endedHunts = startedHuntCollection.find(eq("status", false)).into(new ArrayList<>());
    ctx.json(endedHunts);
    ctx.status(HttpStatus.OK);
  }

  /**
   * Ends a StartedHunt identified by the ID from the Javalin Context.
   * Sets the hunt's status to false, accessCode to "1", and endDate to the
   * current date.
   * If the hunt is not found, a NotFoundResponse is thrown.
   * If successful, the HTTP status is set to OK.
   *
   * @param ctx a Javalin Context object with the HTTP request information.
   */
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

  /**
   * Deletes a StartedHunt identified by the ID from the Javalin Context.
   * If the StartedHunt exists, it retrieves the list of submission IDs and
   * deletes all associated submissions. It also deletes all teams associated
   * with the StartedHunt.
   * Finally, it removes the StartedHunt from the collection.
   * If successful, the HTTP status is set to NO_CONTENT.
   *
   * @param ctx a Javalin Context object with the HTTP request information.
   */
  public void deleteStartedHunt(Context ctx) {
    String id = ctx.pathParam("id");
    StartedHunt startedHunt = startedHuntCollection.find(eq("_id", new ObjectId(id))).first();

    if (startedHunt == null) {
      throw new NotFoundResponse("The started hunt with id " + id + " was not found");
    }

    // Get the list of submissionIds from the startedHunt
    List<String> submissionIds = startedHunt.getSubmissionIds();

    // Delete all submissions with those IDs
    for (String submissionId : submissionIds) {
      Submission submission = submissionController.getSubmission(submissionId);
      if (submission != null) {
        submissionController.deleteSubmission(ctx, submissionId);
      }
    }
    // Delete all teams associated with the StartedHunt
    List<Team> teams = teamController.getTeamsByStartedHuntId(id);
    if (teams != null && !teams.isEmpty()) {
      teamController.deleteTeamsByStartedHuntId(id);
    }

    startedHuntCollection.removeById(id);
    ctx.status(HttpStatus.NO_CONTENT);
  }

  @Override
  public void addRoutes(Javalin server) {
    server.post(API_START_HUNT, this::startHunt);
    server.get(API_STARTED_HUNT, this::getStartedHunt);
    server.get(API_STARTED_HUNTS, this::getStartedHuntsByHostId);
    server.get(API_ENDED_HUNTS, this::getEndedHunts);
    server.get(API_START_HUNT, this::getStartedHuntById);
    server.delete(API_DELETE_HUNT, this::deleteStartedHunt);
    server.put(API_END_HUNT, this::endStartedHunt);
  }

}
