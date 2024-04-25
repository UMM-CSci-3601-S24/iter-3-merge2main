package umm3601.host;

import static com.mongodb.client.model.Filters.eq;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bson.UuidRepresentation;
import org.bson.types.ObjectId;
import org.mongojack.JacksonMongoCollection;

import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.DeleteResult;

import io.javalin.Javalin;
import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import io.javalin.http.NotFoundResponse;
import umm3601.Controller;

public class StartedHuntController implements Controller {

  // private static final String API_HUNT = "/api/hunts/{id}";
  // private static final String API_START_HUNT = "/api/startHunt/{id}";
  private static final String API_STARTED_HUNT = "/api/startedHunts/{accessCode}";
  private static final String API_END_HUNT = "/api/endHunt/{id}";
  private static final String API_ENDED_HUNT = "/api/endedHunts/{id}";
  private static final String API_ENDED_HUNTS = "/api/hosts/{id}/endedHunts";
  private static final String API_DELETE_HUNT = "/api/endedHunts/{id}";
  private static final String API_PHOTO_UPLOAD = "/api/startedHunt/{startedHuntId}/tasks/{taskId}/photo";
  private static final String API_PHOTO_REPLACE = "/api/startedHunt/{startedHuntId}/tasks/{taskId}/photo/{photoId}";

  // private static final int ACCESS_CODE_MIN = 100000;
  // private static final int ACCESS_CODE_RANGE = 900000;
  private static final int ACCESS_CODE_LENGTH = 6;

  static final String HUNT_KEY = "huntId";

  private final JacksonMongoCollection<StartedHunt> startedHuntCollection;

  public StartedHuntController(MongoDatabase database) {
    startedHuntCollection = JacksonMongoCollection.builder().build(
        database,
        "startedHunts",
        StartedHunt.class,
        UuidRepresentation.STANDARD);

    File directory = new File("photos");
    if (!directory.exists()) {
        directory.mkdir();
    }
  }

  public StartedHunt getStartedHunt(Context ctx) {
    String accessCode = ctx.pathParam("accessCode");
    StartedHunt startedHunt;

    // Validate the access code
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

  public void getEndedHunts(Context ctx) {
    List<StartedHunt> endedHunts = startedHuntCollection.find(eq("status", false)).into(new ArrayList<>());
    ctx.json(endedHunts);
    ctx.status(HttpStatus.OK);
  }

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

  public void deleteStartedHunt(Context ctx) {
    String id = ctx.pathParam("id");
    StartedHunt startedHunt = startedHuntCollection.find(eq("_id", new ObjectId(id))).first();
    DeleteResult deleteResult = startedHuntCollection.deleteOne(eq("_id", new ObjectId(id)));
    if (deleteResult.getDeletedCount() != 1) {
      ctx.status(HttpStatus.NOT_FOUND);
      throw new NotFoundResponse(
          "Was unable to delete ID "
              + id
              + "; perhaps illegal ID or an ID for an item not in the system?");
     }
    ctx.status(HttpStatus.OK);

    for (Task task : startedHunt.completeHunt.tasks) {
      for (String photo : task.photos) {
        deletePhoto(photo, ctx);
      }
    }
  }

  public void addPhoto(Context ctx) {
    String id = uploadPhoto(ctx);
    addPhotoPathToTask(ctx, id);
    ctx.status(HttpStatus.CREATED);
    ctx.json(Map.of("id", id));
  }

  public String getFileExtension(String filename) {
    int dotIndex = filename.lastIndexOf('.');
    if (dotIndex >= 0) {
      return filename.substring(dotIndex + 1);
    } else {
      return "";
    }
  }

  public String uploadPhoto(Context ctx) {
    try {
      var uploadedFile = ctx.uploadedFile("photo");
      if (uploadedFile != null) {
        try (InputStream in = uploadedFile.content()) {

          String id = UUID.randomUUID().toString();

          String extension = getFileExtension(uploadedFile.filename());
          File file = Path.of("photos", id + "." + extension).toFile();
          System.err.println("The path was " + file.toPath());

          Files.copy(in, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
          ctx.status(HttpStatus.OK);
          return id + "." + extension;
        } catch (IOException e) {
          System.err.println("Error copying the uploaded file: " + e);
          throw new BadRequestResponse("Error handling the uploaded file: " + e.getMessage());
        }
      } else {
        throw new BadRequestResponse("No photo uploaded");
      }
    } catch (Exception e) {
      throw new BadRequestResponse("Unexpected error during photo upload: " + e.getMessage());
    }
  }

  public void addPhotoPathToTask(Context ctx, String photoPath) {
    String taskId = ctx.pathParam("taskId");
    String startedHuntId = ctx.pathParam("startedHuntId");
    StartedHunt startedHunt = startedHuntCollection.find(eq("_id", new ObjectId(startedHuntId))).first();
    if (startedHunt == null) {
      ctx.status(HttpStatus.NOT_FOUND);
      throw new BadRequestResponse("StartedHunt with ID " + startedHuntId + " does not exist");
    }

    Task task = startedHunt.completeHunt.tasks.stream().filter(t -> t._id.equals(taskId)).findFirst().orElse(null);

    if (task == null) {
      ctx.status(HttpStatus.NOT_FOUND);
      throw new BadRequestResponse("Task with ID " + taskId + " does not exist");
    }

    task.photos.add(photoPath);
    startedHunt.completeHunt.tasks.set(startedHunt.completeHunt.tasks.indexOf(task), task);
    startedHuntCollection.save(startedHunt);
  }

  public void replacePhoto(Context ctx) {
    String startedHuntId = ctx.pathParam("startedHuntId");
    String taskId = ctx.pathParam("taskId");
    String photoId = ctx.pathParam("photoId");
    deletePhoto(photoId, ctx);
    removePhotoPathFromTask(ctx, taskId, startedHuntId, photoId);
    addPhoto(ctx);
  }

  public void deletePhoto(String id, Context ctx) {
    Path filePath = Path.of("photos/" + id);
    if (!Files.exists(filePath)) {
      ctx.status(HttpStatus.NOT_FOUND);
      throw new BadRequestResponse("Photo with ID " + id + " does not exist");
  }

    try {
      Files.delete(filePath);

      ctx.status(HttpStatus.OK);
    } catch (IOException e) {
      ctx.status(HttpStatus.INTERNAL_SERVER_ERROR);
      throw new BadRequestResponse("Error deleting the photo: " + e.getMessage());
    }
  }

  public void removePhotoPathFromTask(Context ctx, String taskId, String startedHuntId, String photoId) {
    StartedHunt startedHunt = startedHuntCollection.find(eq("_id", new ObjectId(startedHuntId))).first();
    if (startedHunt == null) {
      ctx.status(HttpStatus.NOT_FOUND);
      throw new BadRequestResponse("StartedHunt with ID " + startedHuntId + " does not exist");
    }

    Task task = startedHunt.completeHunt.tasks.stream().filter(t -> t._id.equals(taskId)).findFirst().orElse(null);
    if (task == null) {
      ctx.status(HttpStatus.NOT_FOUND);
      throw new BadRequestResponse("Task with ID " + taskId + " does not exist");
    }

    task.photos.remove(photoId);
    startedHunt.completeHunt.tasks.set(startedHunt.completeHunt.tasks.indexOf(task), task);
    startedHuntCollection.save(startedHunt);
  }

  public void getEndedHunt(Context ctx) {
    EndedHunt finishedHunt = new EndedHunt();
    finishedHunt.startedHunt = getStartedHuntById(ctx);
    finishedHunt.finishedTasks = getFinishedTasks(finishedHunt.startedHunt.completeHunt.tasks);

    ctx.json(finishedHunt);
    ctx.status(HttpStatus.OK);
  }

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
      return startedHunt;
    }
  }

  public List<FinishedTask> getFinishedTasks(List<Task> tasks) {
    ArrayList<FinishedTask> finishedTasks = new ArrayList<>();
    FinishedTask finishedTask;
    for (Task task : tasks) {
      finishedTask = new FinishedTask();
      finishedTask.taskId = task._id;
      finishedTask.photos = getPhotosFromTask(task);
      finishedTasks.add(finishedTask);
    }
    return finishedTasks;
  }

  public List<String> getPhotosFromTask(Task task) {
    List<String> encodedPhotos = new ArrayList<>();
    for (String photoPath : task.photos) {
      File photo = new File("photos/" + photoPath);
      if (photo.exists()) {
        try {
          byte[] bytes = Files.readAllBytes(Paths.get(photo.getPath()));
          String encoded = "data:image/png;base64," + Base64.getEncoder().encodeToString(bytes);
          encodedPhotos.add(encoded);
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
    return encodedPhotos;
  }


  @Override
  public void addRoutes(Javalin server) {
    server.get(API_STARTED_HUNT, this::getStartedHunt);
    server.put(API_END_HUNT, this::endStartedHunt);
    server.post(API_PHOTO_UPLOAD, this::addPhoto);
    server.put(API_PHOTO_REPLACE, this::replacePhoto);
    server.get(API_ENDED_HUNT, this::getEndedHunt);
    server.get(API_ENDED_HUNTS, this::getEndedHunts);
    server.delete(API_DELETE_HUNT, this::deleteStartedHunt);
  }
}
