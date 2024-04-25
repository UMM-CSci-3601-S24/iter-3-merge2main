import { HttpClient } from "@angular/common/http";
import { TeamService } from "./team.service";
import { HttpClientTestingModule, HttpTestingController } from "@angular/common/http/testing";
import { Team } from "./team";
import { TestBed } from "@angular/core/testing";

describe('TeamService', () => {
  const testTeams: Team[] = [
    {
      _id: '1234',
      teamName: 'Team 1',
      startedHuntId: '5678',
    },
    {
      _id: '5432',
      teamName: 'Team 2',
      startedHuntId: '8765',
    },
    {
      _id: '9876',
      teamName: 'Team 3',
      startedHuntId: '5678', // Same startedHuntId as Team 1
    }
  ];

  let teamService: TeamService;
  let httpClient: HttpClient;
  let httpTestingController: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule]
    });

    httpClient = TestBed.inject(HttpClient);
    httpTestingController = TestBed.inject(HttpTestingController);
    teamService = new TeamService(httpClient);
  });

  afterEach(() => {
    httpTestingController.verify();
  });

  it('should be created', () => {
    expect(teamService).toBeTruthy();
  });

  it('should get all teams', () => {
    teamService.getTeams().subscribe(teams => {
      expect(teams).toBe(testTeams);
    });

    const req = httpTestingController.expectOne(`${teamService.teamUrl}`);
    expect(req.request.method).toEqual('GET');
    req.flush(testTeams);
  });

  it('should get a team by id', () => {
    const teamId = '1234';
    const team = testTeams.find(t => t._id === teamId);

    teamService.getTeam(teamId).subscribe(t => {
      expect(t).toBe(team);
    });

    const req = httpTestingController.expectOne(`${teamService.teamUrl}/${teamId}`);
    expect(req.request.method).toEqual('GET');
    req.flush(team);
  });

  it('should create a team', () => {
    const newTeam: Team = {
      _id: '1357',
      teamName: 'Team 4',
      startedHuntId: '2468'
    };

    teamService.createTeam(newTeam.teamName, newTeam.startedHuntId).subscribe(id => {
      expect(id).toBe(newTeam._id);
    });

    const req = httpTestingController.expectOne(`${teamService.teamUrl}`);
    expect(req.request.method).toEqual('POST');
    req.flush({ id: newTeam._id });
  });

  it('should delete a team', () => {
    const teamId = '1234';

    teamService.deleteTeam(teamId).subscribe();

    const req = httpTestingController.expectOne(`${teamService.teamUrl}/${teamId}`);
    expect(req.request.method).toEqual('DELETE');
    req.flush({});
  });

  it('should create teams', () => {
    const startedHuntId = '5678';
    const numTeams = 2;

    teamService.createTeams(startedHuntId, numTeams).subscribe();

    const req = httpTestingController.expectOne(`${teamService.teamUrl}/create`);
    expect(req.request.method).toEqual('POST');
    req.flush({});
  });

  it('should get all teams for a started hunt', () => {
    const startedHuntId = '5678';
    const startedHuntTeams = testTeams.filter(t => t.startedHuntId === startedHuntId);

    teamService.getAllStartedHuntTeams(startedHuntId).subscribe(teams => {
      expect(teams).toBe(startedHuntTeams);
    });

    const req = httpTestingController.expectOne(`${teamService.teamUrl}/startedHunt/${startedHuntId}`);
    expect(req.request.method).toEqual('GET');
    req.flush(startedHuntTeams);
  });

});
