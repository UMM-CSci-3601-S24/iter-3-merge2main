package umm3601.teams;

import static com.mongodb.client.model.Filters.eq;

import java.util.ArrayList;
import java.util.Map;

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

public class TeamController implements Controller {

  private static final String API_TEAM = "/api/teams/{id}";
  private static final String API_TEAMS = "/api/teams";
  private static final String API_CREATE_TEAMS = "/api/teams/create";

  private final JacksonMongoCollection<Team> teamCollection;

  public TeamController(MongoDatabase database) {

    teamCollection = JacksonMongoCollection.builder().build(
        database,
        "teams",
        Team.class,
        UuidRepresentation.STANDARD);
  }

  /**
   * Creates a new team.
   *
   * @param ctx a Javalin Context object containing the HTTP request information.
   *            Expects a request body that can be validated as a Team object.
   *            The team name is required and must be non-empty.
   *
   *            The method creates a new Team object from the request body and
   *            inserts it into the team collection in the database.
   *            It then responds with a JSON object containing the ID of the newly
   *            created team and sets the HTTP status to 201 (Created).
   */
  public void createTeam(Context ctx) {
    Team newTeam = ctx.bodyValidator(Team.class)
        .check(team -> team.teamName != null && team.teamName.length() > 0, "Team name is required")
        .get();

    teamCollection.insertOne(newTeam);
    ctx.json(Map.of("id", newTeam._id));
    ctx.status(HttpStatus.CREATED);
  }

  /**
   * Retrieves a team by its ID.
   *
   * @param ctx a Javalin Context object containing the HTTP request information.
   *            Expects a path parameter 'id' representing the team's ID.
   *
   *            The method attempts to find a team in the database with the
   *            provided ID.
   *            If the ID is not a valid Mongo Object ID, it throws a
   *            BadRequestResponse.
   *            If no team is found with the provided ID, it throws a
   *            NotFoundResponse.
   *            If a team is found, it responds with the team in JSON format and
   *            sets the HTTP status to 200 (OK).
   */
  public void getTeam(Context ctx) {
    String id = ctx.pathParam("id");
    Team team;

    try {
      team = teamCollection.find(eq("_id", new ObjectId(id))).first();
    } catch (IllegalArgumentException e) {
      throw new BadRequestResponse("The requested team id wasn't a legal Mongo Object ID.");
    }

    if (team == null) {
      throw new NotFoundResponse("The requested team was not found");
    } else {
      ctx.json(team);
      ctx.status(HttpStatus.OK);
    }
  }

  /**
   * Retrieves all teams.
   *
   * @param ctx a Javalin Context object containing the HTTP request information.
   *
   *            The method attempts to find all teams in the database and convert
   *            the result into an ArrayList.
   *            If successful, it responds with the teams in JSON format and sets
   *            the HTTP status to 200 (OK).
   *            If an exception occurs during the process, it responds with a
   *            status of 500 (Internal Server Error) and a message indicating an
   *            error occurred while retrieving teams from the database.
   */
  public void getTeams(Context ctx) {
    try {
      ArrayList<Team> teams = teamCollection.find().into(new ArrayList<>());
      ctx.json(teams);
      ctx.status(HttpStatus.OK);
    } catch (Exception e) {
      ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).result("Error retrieving teams from database");
    }
  }

  @Override
  public void addRoutes(Javalin server) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'addRoutes'");
  }

}
