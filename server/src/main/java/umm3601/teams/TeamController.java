package umm3601.teams;

import static com.mongodb.client.model.Filters.eq;

import java.util.ArrayList;
import java.util.List;
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

  private static final String API_SINGLE_TEAM = "/api/teams/{teamName}";
  private static final String API_TEAM = "/api/teams/{id}";
  private static final String API_TEAMS = "/api/teams";
  private static final String API_CREATE_TEAMS = "/api/teams/create";
  private static final String API_STARTEDHUNT_TEAMS = "/api/teams/startedHunt/{startedHuntId}";

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

  /**
   * Deletes a team by its ID.
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
   *            If a team is found, it deletes the team from the database and sets
   *            the HTTP status to 204 (No Content).
   */
  public void deleteTeam(Context ctx) {
    String id = ctx.pathParam("id");
    try {
      Team team = teamCollection.find(eq("_id", new ObjectId(id))).first();
      if (team == null) {
        throw new NotFoundResponse("The requested team was not found");
      } else {
        teamCollection.deleteOne(eq("_id", new ObjectId(id)));
        ctx.status(HttpStatus.NO_CONTENT);
      }
    } catch (IllegalArgumentException e) {
      throw new BadRequestResponse("The requested team id wasn't a legal Mongo Object ID.");
    }
  }

  /**
   * Creates a specified number of teams for a given hunt.
   *
   * @param ctx the Javalin context, which includes the request and response
   *            objects
   *
   *            The method retrieves the startedHunt ID and the number of teams to
   *            be created from the request body.
   *            It validates the number of teams to be between 1 and 10.
   *            For each team to be created, it creates a new Team object, sets
   *            the team name and startedHunt ID, and adds it to a list of teams.
   *            It then attempts to insert all the teams into the team collection
   *            in the database.
   *            If successful, it sets the response status to 201 (Created).
   *            If an exception occurs during the process, it sets the response
   *            status to 500 (Internal Server Error) and includes an error
   *            message in the response.
   */
  public void createTeams(Context ctx) {
    String startedHuntId = ctx.pathParam("startedHuntId");
    int numTeams = ctx.bodyAsClass(Integer.class);

    if (numTeams < 1 || numTeams > 10) {
      throw new BadRequestResponse("Invalid number of teams requested");
    }

    List<Team> teams = new ArrayList<>();
    for (int i = 0; i < numTeams; i++) {
      Team newTeam = new Team();
      newTeam.teamName = "Team " + i;
      newTeam.startedHuntId = startedHuntId;
      teams.add(newTeam);
    }

    try {
      teamCollection.insertMany(teams);
      ctx.status(HttpStatus.CREATED);
    } catch (Exception e) {
      ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).result("Error creating teams");
    }
  }

  /**
   * Retrieves all teams associated with a specific started hunt.
   *
   * @param ctx the Javalin context, which includes the request and response
   *            objects
   *
   *            The method retrieves the hunt ID from the path parameters of the
   *            request.
   *            It then queries the team collection in the database for all teams
   *            where the 'startedHuntId' matches the provided hunt ID.
   *            The result is a list of Team objects, which is converted into JSON
   *            and included in the response body.
   *            The response status is set to 200 (OK).
   */
  public void getAllStartedHuntTeams(Context ctx) {
    String startedHuntId = ctx.pathParam("startedHuntId");

    List<Team> teams = teamCollection.find(eq("startedHuntId", startedHuntId)).into(new ArrayList<>());

    ctx.json(teams);
    ctx.status(HttpStatus.OK);
  }

  /**
   * Retrieves all teams associated with a specific started hunt.
   *
   * @param startedHuntId the ID of the started hunt
   * @return an ArrayList of Team objects associated with the given started hunt
   *
   *         The method takes a hunt ID as input.
   *         It queries the team collection in the database for all teams where
   *         the 'startedHuntId' matches the provided hunt ID.
   *         The result is an ArrayList of Team objects.
   */
  public ArrayList<Team> getTeamsByStartedHuntId(String startedHuntId) {
    return teamCollection.find(eq("startedHuntId", startedHuntId)).into(new ArrayList<>());
  }

  @Override
  public void addRoutes(Javalin server) {
    server.post(API_SINGLE_TEAM, this::createTeam);
    server.get(API_TEAM, this::getTeam);
    server.get(API_TEAMS, this::getTeams);
    server.delete(API_TEAM, this::deleteTeam);
    server.post(API_CREATE_TEAMS, this::createTeams);
    server.get(API_STARTEDHUNT_TEAMS, this::getAllStartedHuntTeams);
  }
}
