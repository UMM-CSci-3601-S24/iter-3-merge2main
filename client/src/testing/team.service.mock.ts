import { Injectable } from "@angular/core";
import { Observable, of } from "rxjs";
import { AppComponent } from "src/app/app.component";
import { Team } from "src/app/teams/team";
import { TeamService } from "src/app/teams/team.service";

@Injectable({
  providedIn: AppComponent,
})
export class MockTeamService extends TeamService {
  static testTeams: Team[] = [
    {
      _id: 'team1_id',
      teamName: 'Team 1',
      startedHuntId: 'startedHunt1_id',
      selected: false,
    },
    {
      _id: 'team2_id',
      teamName: 'Team 2',
      startedHuntId: 'startedHunt1_id',
      selected: false,
    },
  ];

  constructor() {
    super(null);
  }

  createTeam(teamName: string, startedHuntId: string): Observable<string> {
    console.log('Creating team ' + teamName + ' for started hunt ' + startedHuntId + '...');
    return of('team3_id');
  }

  getTeam(id: string): Observable<Team> {
    return of(MockTeamService.testTeams.find(team => team._id === id));
  }

  getTeams(): Observable<Team[]> {
    return of(MockTeamService.testTeams);
  }

  getAllStartedHuntTeams(startedHuntId: string): Observable<Team[]> {
    console.log('Getting all teams for started hunt ' + startedHuntId + '...');
    return of(MockTeamService.testTeams.filter(team => team.startedHuntId === startedHuntId));
  }

  addTeams(startedHuntId: string, numTeams: number): Observable<void> {
    console.log('Adding ' + numTeams + ' teams for started hunt ' + startedHuntId + '...');
    return of(null);
  }
}
