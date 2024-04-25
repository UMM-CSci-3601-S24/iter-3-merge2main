package umm3601.teams;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import org.bson.UuidRepresentation;
import org.bson.types.ObjectId;
import org.mongojack.JacksonMongoCollection;

import com.mongodb.client.MongoDatabase;
import static com.mongodb.client.model.Filters.eq;

import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import umm3601.Controller;

public class SubmissionController implements Controller {

  private static final String API_SUBMISSION = "/api/submissions/{id}";
  private static final String API_SUBMISSIONS_BY_TEAM = "/api/submissions/team/{teamId}";
  private static final String API_SUBMISSIONS_BY_TASK = "/api/submissions/task/{taskId}";
  private static final String API_SUBMISSIONS_BY_TEAM_AND_TASK = "/api/submissions/team/{teamId}/task/{taskId}";
  private static final String API_SUBMISSIONS_BY_STARTEDHUNT = "/api/submissions/startedHunt/{startedHuntId}";
  private static final String API_SUBMISSION_GET_PHOTO = "/api/submissions/{id}/photo";

  private final JacksonMongoCollection<Submission> submissionCollection;

  public SubmissionController(MongoDatabase database) {

    submissionCollection = JacksonMongoCollection.builder().build(
        database,
        "submissions",
        Submission.class,
        UuidRepresentation.STANDARD);
  }

  /**
   * Retrieves a Submission from the database.
   *
   * @param ctx a Javalin Context object containing the HTTP request information.
   *            Expects an "id" path parameter.
   * @return the Submission object with the matching ID, or null if no such
   *         Submission exists.
   */
  public Submission getSubmission(Context ctx) {
    String id = ctx.pathParam("id");
    return submissionCollection.find(eq("_id", new ObjectId(id))).first();
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
    List<Submission> teamSubmissions = submissionCollection.find(eq("teamId", teamId)).into(new ArrayList<>());
    ctx.json(teamSubmissions);
    ctx.status(HttpStatus.OK);
  }

  /**
   * Retrieves the first Submission associated with a specific task from the
   * database.
   *
   * @param ctx a Javalin Context object containing the HTTP request information.
   *            Expects a "taskId" path parameter.
   * @return the first Submission object with the matching taskId, or null if no
   *         such Submission exists.
   */
  public Submission getSubmissionsByTask(Context ctx) {
    String taskId = ctx.pathParam("taskId");
    return submissionCollection.find(eq("taskId", taskId)).first();
  }

  /**
   * Retrieves the first Submission associated with a specific team and task from
   * the database.
   *
   * @param ctx a Javalin Context object containing the HTTP request information.
   *            Expects "teamId" and "taskId" path parameters.
   * @return the first Submission object with the matching teamId and taskId, or
   *         null if no such Submission exists.
   */
  public Submission getSubmissionsByTeamAndTask(Context ctx) {
    String teamId = ctx.pathParam("teamId");
    String taskId = ctx.pathParam("taskId");

    return submissionCollection.find(eq("teamId", teamId)).filter(eq("taskId", taskId)).first();
  }

  /**
   * Retrieves the first Submission associated with a specific started hunt from
   * the database.
   *
   * @param ctx a Javalin Context object containing the HTTP request information.
   *            Expects a "startedHuntId" path parameter.
   * @return the first Submission object with the matching startedHuntId, or null
   *         if no such Submission exists.
   */
  public Submission getSubmissionsByStartedHunt(Context ctx) {
    String startedHuntId = ctx.pathParam("startedHuntId");

    return submissionCollection.find(eq("startedHuntId", startedHuntId)).first();
  }

  /**
   * Retrieves a photo associated with a specific submission.
   *
   * @param ctx a Javalin Context object containing the HTTP request information.
   *            Expects an "id" path parameter representing the submissionId.
   *
   *            The method first retrieves the Submission object associated with
   *            the provided submissionId from the database. It then attempts to
   *            locate a photo file using the photoPath attribute of the
   *            Submission object.
   *            If the photo file exists, it sets a FileInputStream of the photo
   *            as the result of the context.
   *            If the photo file does not exist, it sets an empty string as the
   *            result of the context.
   */
  public void getPhotoFromSubmission(Context ctx) {
    String submissionId = ctx.pathParam("id");
    System.out.println("Server: Received request to get photo for submissionId: " + submissionId);

    Submission submission = submissionCollection.find(eq("_id", new ObjectId(submissionId))).first();

    File photo = new File("photos/" + submission.photoPath);
    if (photo.exists()) {
      try {
        ctx.result(new FileInputStream(photo));
      } catch (FileNotFoundException e) {
        ctx.status(500).result("Error reading file: " + e.getMessage());
      }
    } else {
      System.out.println("Server: No photo found for submissionId: " + submissionId);
      ctx.result("");
    }
  }

  @Override
  public void addRoutes(Javalin server) {
    server.get(API_SUBMISSION, this::getSubmission);
    server.get(API_SUBMISSIONS_BY_TEAM, this::getSubmissionsByTeam);
    server.get(API_SUBMISSIONS_BY_TASK, this::getSubmissionsByTask);
    server.get(API_SUBMISSIONS_BY_TEAM_AND_TASK, this::getSubmissionsByTeamAndTask);
    server.get(API_SUBMISSIONS_BY_STARTEDHUNT, this::getSubmissionsByStartedHunt);
    server.get(API_SUBMISSION_GET_PHOTO, this::getPhotoFromSubmission);
  }

}
