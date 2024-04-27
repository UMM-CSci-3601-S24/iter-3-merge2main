import { HttpClient } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { Observable } from "rxjs";
import { environment } from "src/environments/environment";
import { StartedHunt } from "./startedHunt";

@Injectable({
  providedIn: 'root'
})
export class StartedHuntService {
  readonly startHuntUrl: string = `${environment.apiUrl}startHunt`;
  readonly startedHuntsUrl: string = `${environment.apiUrl}startedHunts`;
  readonly endHuntUrl: string = `${environment.apiUrl}endHunt`;
  readonly endedHuntsUrl: string = `${environment.apiUrl}endedHunts`;

  constructor(private httpClient: HttpClient) {
  }

  startHunt(id: string): Observable<string> {
    return this.httpClient.get<string>(`${this.startHuntUrl}/${id}`);
  }

  getStartedHunt(accessCode: string): Observable<StartedHunt> {
    return this.httpClient.get<StartedHunt>(`${this.startedHuntsUrl}/${accessCode}`);
  }

  getEndedHuntById(id: string): Observable<StartedHunt> {
    return this.httpClient.get<StartedHunt>(`${this.endedHuntsUrl}/${id}`);
  }

  getStartedHuntById(id: string): Observable<StartedHunt> {
    return this.httpClient.get<StartedHunt>(`${this.startedHuntsUrl}/${id}`);
  }

  endStartedHunt(id: string): Observable<void> {
    return this.httpClient.put<void>(`${this.endHuntUrl}/${id}`, null);
  }

  getEndedHunts(): Observable<StartedHunt[]> {
    return this.httpClient.get<StartedHunt[]>(`${this.endedHuntsUrl}`);
  }
}
