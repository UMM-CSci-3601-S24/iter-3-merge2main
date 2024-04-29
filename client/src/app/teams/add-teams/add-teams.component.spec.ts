
import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { AddTeamsComponent } from './add-teams.component';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ActivatedRoute, Router } from '@angular/router';
import { HostService } from 'src/app/hosts/host.service';
import { StartedHuntService } from 'src/app/startHunt/startedHunt.service';
import { TeamService } from '../team.service';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { StartedHunt } from 'src/app/startHunt/startedHunt';
import { ActivatedRouteStub } from 'src/testing/activated-route-stub';
import { MockStartedHuntService } from 'src/testing/startedHunt.service.mock';
import { MockTeamService } from 'src/testing/team.service.mock';
import { HttpClientModule } from '@angular/common/http';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { MatCardModule } from '@angular/material/card';
import { RouterTestingModule } from '@angular/router/testing';

describe('AddTeamsComponent', () => {
  let component: AddTeamsComponent;
  let fixture: ComponentFixture<AddTeamsComponent>;
  const mockStartedHuntService = new MockStartedHuntService();
  const mockTeamService = new MockTeamService();
  const accessCode = "123456"
  const activatedRoute: ActivatedRouteStub = new ActivatedRouteStub({
    accessCode : accessCode
  });
  const snackBarSpy = jasmine.createSpyObj('MatSnackBar', ['open', 'dismiss']);
  const hostServiceSpy = jasmine.createSpyObj('HostService', ['getHost']);
  const routerSpy = jasmine.createSpyObj('Router', ['navigate']);

  beforeEach(waitForAsync(() => {
     TestBed.configureTestingModule({
      imports: [
        FormsModule,
        CommonModule,
        HttpClientModule,
        RouterTestingModule,
        MatCardModule,
        HttpClientModule,
        HttpClientTestingModule
      ],
      providers: [
        { provide: MatSnackBar, useValue: snackBarSpy },
        { provide: ActivatedRoute, useValue: activatedRoute },
        { provide: HostService, useValue: hostServiceSpy },
        { provide: StartedHuntService, useValue: mockStartedHuntService },
        { provide: TeamService, useValue: mockTeamService },
        { provide: Router, useValue: routerSpy }
      ]
    })

    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AddTeamsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize component properties', () => {
    expect(component.numTeams).toBe(1);
    expect(component.startedHunt).toBeUndefined();
    expect(component.teams).toEqual([]);
    expect(component.accessCode).toBeUndefined();
    expect(component.startedHuntId).toBeUndefined();
  });

  it('should call open method on error adding teams', () => {
    const id = 'abc123';
    const numTeams = 2;
    component.startedHunt = null;

    component.addTeams(id, numTeams);

    expect(snackBarSpy.open).toHaveBeenCalledWith('Error adding teams', 'Close', { duration: 3000 });
  });

  it('should call open method on success adding teams', () => {
    const id = 'abc123';
    const numTeams = 2;
    component.startedHunt = { _id: '123', accessCode: 'abc123' } as StartedHunt;

    component.addTeams(id, numTeams);

    expect(snackBarSpy.open).toHaveBeenCalledWith('Teams added successfully', 'Close', { duration: 3000 });
    expect(routerSpy.navigate).toHaveBeenCalledWith([`/startedHunts/${component.startedHunt.accessCode}`]);
  });
});
