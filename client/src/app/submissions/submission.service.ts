import { HttpClient } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { Observable } from "rxjs";
import { environment } from "src/environments/environment";
import { Submission } from "./submission";

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

  getSubmissionsByTeam(teamId: string): Observable<Submission[]> {
    return this.httpClient.get<Submission[]>(`${this.submissionUrl}/team/${teamId}`);
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
    return this.httpClient.get(`${this.submissionUrl}/${id}/photo`, {responseType: 'text'});
  }

  deleteSubmission(id: string) {
    return this.httpClient.delete(`${this.submissionUrl}/${id}`);
  }

  submitPhoto(startedHuntId: string, teamId: string, taskId: string, photo: File) {
    const formData = new FormData();
    formData.append('photo', photo);
    return this.httpClient.post(`${this.submissionUrl}/startedHunt/${startedHuntId}/team/${teamId}/task/${taskId}`, formData);
  }

  replacePhoto(teamId: string, taskId: string, photo: File) {
    const formData = new FormData();
    formData.append('photo', photo);
    return this.httpClient.put(`${this.submissionUrl}/team/${teamId}/task/${taskId}`, formData);
  }

  getPhotoUrl(photoPath: string) {
    return `${environment.apiUrl}submissions/photo/${photoPath}`;
  }
}
