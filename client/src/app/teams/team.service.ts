import { HttpClient } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { Observable, map } from "rxjs";
import { environment } from "src/environments/environment";
import { Team } from "./team";

@Injectable({
  providedIn: 'root'
})
export class TeamService {
  readonly teamUrl: string = `${environment.apiUrl}teams`;

  constructor(private httpClient: HttpClient) {
  }

  createTeam(teamName: string, startedHuntId: string): Observable<string> {
    return this.httpClient.post<{ id: string }>(`${this.teamUrl}`, { teamName, startedHuntId }).pipe(map(result => result.id));
  }

  getTeam(id: string): Observable<Team> {
    return this.httpClient.get<Team>(`${this.teamUrl}/${id}`);
  }

  getTeams(): Observable<Team[]> {
    return this.httpClient.get<Team[]>(`${this.teamUrl}`);
  }

  deleteTeam(id: string): Observable<void> {
    return this.httpClient.delete<void>(`${this.teamUrl}/${id}`);
  }

  addTeams(startedHuntId: string, numTeams: number): Observable<void> {
    return this.httpClient.post<void>(`${this.teamUrl}/addTeams/${startedHuntId}/${numTeams}`, null);
  }

  getAllStartedHuntTeams(startedHuntId: string): Observable<Team[]> {
    return this.httpClient.get<Team[]>(`${this.teamUrl}/startedHunt/${startedHuntId}`);
  }
}
