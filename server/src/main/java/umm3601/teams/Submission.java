package umm3601.teams;

import java.util.Date;

import org.mongojack.Id;
import org.mongojack.ObjectId;

public class Submission {

  @ObjectId
  @Id
  @SuppressWarnings({ "MemberName" })
  public String _id;

  public String taskId;
  public String teamId;
  public String photoPath;
  public Date submitTime;
}
