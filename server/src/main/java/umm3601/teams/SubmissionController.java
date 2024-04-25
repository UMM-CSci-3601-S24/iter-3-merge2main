package umm3601.teams;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.io.FileInputStream;
import java.io.IOException;

import org.bson.UuidRepresentation;
import org.bson.types.ObjectId;
import org.mongojack.JacksonMongoCollection;

import com.mongodb.client.MongoDatabase;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.in;

import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import umm3601.Controller;
import umm3601.host.StartedHunt;

public class SubmissionController implements Controller {

  private static final String API_SUBMISSION = "/api/submissions/{id}";
  private static final String API_SUBMISSIONS_BY_TEAM = "/api/submissions/team/{teamId}";
  private static final String API_SUBMISSIONS_BY_TASK = "/api/submissions/task/{taskId}";
  private static final String API_SUBMISSIONS_BY_TEAM_AND_TASK = "/api/submissions/team/{teamId}/task/{taskId}";
  private static final String API_SUBMISSIONS_BY_STARTEDHUNT = "/api/submissions/startedHunt/{startedHuntId}";
  private static final String API_SUBMISSION_GET_PHOTO = "/api/submissions/{id}/photo";

  private final JacksonMongoCollection<Submission> submissionCollection;
  private final JacksonMongoCollection<StartedHunt> startedHuntCollection;

  public SubmissionController(MongoDatabase database) {

    submissionCollection = JacksonMongoCollection.builder().build(
        database,
        "submissions",
        Submission.class,
        UuidRepresentation.STANDARD);

    startedHuntCollection = JacksonMongoCollection.builder().build(
        database,
        "startedHunts",
        StartedHunt.class,
        UuidRepresentation.STANDARD);
  }

  /**
   * Creates a new Submission and inserts it into the submission collection.
   *
   * @param taskId    The ID of the task for the submission.
   * @param teamId    The ID of the team making the submission.
   * @param photoPath The path to the photo for the submission.
   * @return The newly created Submission object.
   */
  public Submission createSubmission(String taskId, String teamId, String photoPath) {
    Submission submission = new Submission();
    submission.taskId = taskId;
    submission.teamId = teamId;
    submission.photoPath = photoPath;
    submission.submitTime = new java.util.Date();
    submissionCollection.insertOne(submission);
    return submission;
  }

  /**
   * Retrieves a Submission from the database and sends it as a JSON response.
   *
   * @param ctx a Javalin Context object containing the HTTP request information.
   *            Expects an "id" path parameter.
   *            If a Submission with the matching ID is found, it is sent as a
   *            JSON response with a status of 200 OK.
   */
  public void getSubmission(Context ctx) {
    String id = ctx.pathParam("id");
    Submission submission = submissionCollection.find(eq("_id", new ObjectId(id))).first();
    if (submission != null) {
      ctx.status(HttpStatus.OK);
      ctx.json(submission);
    }
  }

  /**
   * Retrieves all Submissions associated with a specific team from the database.
   *
   * @param ctx a Javalin Context object containing the HTTP request information.
   *            Expects a "teamId" path parameter.
   *            Outputs a JSON array of Submission objects and sets the HTTP
   *            status to OK (200).
   */
  public void getSubmissionsByTeam(Context ctx) {
    String teamId = ctx.pathParam("teamId");
    ArrayList<Submission> teamSubmissions = submissionCollection.find(eq("teamId", teamId)).into(new ArrayList<>());
    ctx.json(teamSubmissions);
    ctx.status(HttpStatus.OK);
  }

  /**
   * Retrieves all Submissions associated with a specific task from the
   * database.
   *
   * @param ctx a Javalin Context object containing the HTTP request information.
   *            Expects a "taskId" path parameter.
   *            Outputs a JSON array of Submission objects and sets the HTTP
   *            status to OK (200).
   */
  public void getSubmissionsByTask(Context ctx) {
    String taskId = ctx.pathParam("taskId");
    List<Submission> taskSubmissions = submissionCollection.find(eq("taskId", taskId)).into(new ArrayList<>());
    ctx.json(taskSubmissions);
    ctx.status(HttpStatus.OK);
  }

  /**
   * Retrieves the first Submission associated with a specific team and task from
   * the database.
   *
   * @param ctx a Javalin Context object containing the HTTP request information.
   *            Expects "teamId" and "taskId" path parameters.
   *            Outputs a JSON object of the first Submission with the matching
   *            teamId and taskId,
   *            or sets the HTTP status to NOT_FOUND (404) if no such Submission
   *            exists.
   */
  public void getSubmissionByTeamAndTask(Context ctx) {
    String teamId = ctx.pathParam("teamId");
    String taskId = ctx.pathParam("taskId");

    Submission submission = submissionCollection.find(eq("teamId", teamId)).filter(eq("taskId", taskId)).first();
    if (submission != null) {
      ctx.json(submission);
      ctx.status(HttpStatus.OK);
    } else {
      ctx.status(HttpStatus.NOT_FOUND);
    }
  }

  /**
   * Retrieves all Submissions associated with a specific started hunt from
   * the database.
   *
   * @param ctx a Javalin Context object containing the HTTP request information.
   *            Expects a "startedHuntId" path parameter.
   *            Outputs a JSON array of Submission objects with the matching
   *            startedHuntId,
   *            or an empty array if no such Submissions exist.
   */
  public void getSubmissionsByStartedHunt(Context ctx) {
    String startedHuntId = ctx.pathParam("startedHuntId");

    // Check if the startedHuntId is valid
    if (!ObjectId.isValid(startedHuntId)) {
      throw new IllegalArgumentException("Invalid startedHuntId: " + startedHuntId);
    }

    // Fetch the startedHunt object
    StartedHunt startedHunt = startedHuntCollection.find(eq("_id", new ObjectId(startedHuntId))).first();

    if (startedHunt == null) {
      ctx.json(new ArrayList<>());
      ctx.status(HttpStatus.OK);
    } else {
      // Get the list of submissionIds from the startedHunt
      List<String> submissionIds = startedHunt.getSubmissionIds();

      // Fetch all submissions with those IDs
      List<Submission> submissions = submissionCollection
          .find(in("_id", submissionIds.stream().map(ObjectId::new).collect(Collectors.toList())))
          .into(new ArrayList<>());

      ctx.json(submissions);
      ctx.status(HttpStatus.OK);
    }
  }

  /**
   * Retrieves a photo associated with a specific submission from the server.
   *
   * @param ctx a Javalin HTTP context
   *
   *            This method takes a Javalin HTTP context as input, which should
   *            contain a path parameter 'id' representing the submission ID.
   *            It attempts to find a submission with the given ID in the
   *            submission collection, and if found, it tries to retrieve the
   *            associated photo file from the 'photos/' directory.
   *            If the photo file exists, it sends the file as a result with an
   *            HTTP status of OK.
   *            If the photo file does not exist, it sends an empty result with an
   *            HTTP status of NOT FOUND.
   *            If there's an error while accessing the file, it sends an error
   *            message as a result with an HTTP status of INTERNAL SERVER ERROR.
   */
  public void getPhotoFromSubmission(Context ctx) {
    String submissionId = ctx.pathParam("id");
    System.out.println("Server: Received request to get photo for submissionId: " + submissionId);

    Submission submission = submissionCollection.find(eq("_id", new ObjectId(submissionId))).first();

    File photo = new File("photos/" + submission.photoPath);
    if (photo.exists()) {
      try (FileInputStream fis = new FileInputStream(photo)) {
        ctx.result(fis);
        ctx.status(HttpStatus.OK);
      } catch (IOException e) {
        ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).result("Error accessing file: " + e.getMessage());
      }
    } else {
      System.out.println("Server: No photo found for submissionId: " + submissionId);
      ctx.result("");
      ctx.status(HttpStatus.NOT_FOUND);
    }
  }

  @Override
  public void addRoutes(Javalin server) {
    server.get(API_SUBMISSION, this::getSubmission);
    server.get(API_SUBMISSIONS_BY_TEAM, this::getSubmissionsByTeam);
    server.get(API_SUBMISSIONS_BY_TASK, this::getSubmissionsByTask);
    server.get(API_SUBMISSIONS_BY_TEAM_AND_TASK, this::getSubmissionByTeamAndTask);
    server.get(API_SUBMISSIONS_BY_STARTEDHUNT, this::getSubmissionsByStartedHunt);
    server.get(API_SUBMISSION_GET_PHOTO, this::getPhotoFromSubmission);
  }

}
