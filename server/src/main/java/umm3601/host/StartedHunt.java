package umm3601.host;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.mongojack.Id;
import org.mongojack.ObjectId;

@SuppressWarnings({ "VisibilityModifier" })
public class StartedHunt {

  @ObjectId
  @Id
  @SuppressWarnings({ "MemberName" })
  public String _id;
  public String accessCode;
  public CompleteHunt completeHunt;
  public Boolean status;
  public Date endDate;
  public List<String> submissionIds;

  public StartedHunt() {
    submissionIds = new ArrayList<>();
  }

  /**
   * This method doesn't take any input. It returns the list of submission IDs
   * associated with the current instance of the StartedHunt class.
   *
   * @return List<String> - The list of submission IDs.
   */
  public List<String> getSubmissionIds() {
    return this.submissionIds;
  }
}
