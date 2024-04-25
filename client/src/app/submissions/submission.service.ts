import { HttpClient } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { environment } from "src/environments/environment";

@Injectable({
  providedIn: 'root'
})
export class SubmissionService {
  readonly submissionUrl: string = `${environment.apiUrl}submissions`;

  constructor(private httpClient: HttpClient) {
  }

  getSubmission(id: string) {
    return this.httpClient.get(`${this.submissionUrl}/${id}`);
  }

  getSubmissionsByTeam(teamId: string) {
    return this.httpClient.get(`${this.submissionUrl}/team/${teamId}`);
  }

  getSubmissionsByTask(taskId: string) {
    return this.httpClient.get(`${this.submissionUrl}/task/${taskId}`);
  }

  getSubmissionsByTeamAndTask(teamId: string, taskId: string) {
    return this.httpClient.get(`${this.submissionUrl}/team/${teamId}/task/${taskId}`);
  }

  getSubmissionsByStartedHunt(startedHuntId: string) {
    return this.httpClient.get(`${this.submissionUrl}/startedHunt/${startedHuntId}`);
  }

  getPhotoFromSubmission(id: string) {
    return this.httpClient.get(`${this.submissionUrl}/${id}/photo`);
  }
}
