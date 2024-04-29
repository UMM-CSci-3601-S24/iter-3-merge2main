import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { MockHostService } from 'src/testing/host.service.mock';
import { EndedHuntDetailsComponent } from './ended-hunt.details.component';
import { HostService } from 'src/app/hosts/host.service';
import { HttpClientModule } from '@angular/common/http';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { MatCardModule } from '@angular/material/card';
import { RouterTestingModule } from '@angular/router/testing';
import { EndedHuntCardComponent } from '../ended-hunt-card.component';
import { ActivatedRoute } from '@angular/router';
import { ActivatedRouteStub } from 'src/testing/activated-route-stub';
import { MatDialog } from '@angular/material/dialog';
import { PhotoDialogComponent } from './photo-dialog/photo-dialog.component';
import { StartedHunt } from 'src/app/startHunt/startedHunt';
import { StartedHuntService } from 'src/app/startHunt/startedHunt.service';
import { MockStartedHuntService } from 'src/testing/startedHunt.service.mock';
//import { of } from 'rxjs';
import { SubmissionService } from 'src/app/submissions/submission.service';
import { TeamService } from 'src/app/teams/team.service';
//import { MockTeamService } from 'src/testing/team.service.mock';
import { Submission } from 'src/app/submissions/submission';

describe('EndedHuntDetailsComponent', () => {
  let component: EndedHuntDetailsComponent;
  let fixture: ComponentFixture<EndedHuntDetailsComponent>;
  const mockHostService = new MockHostService();
  const mockStartedHuntService = new MockStartedHuntService();
  const chrisId = 'chris_id';
  const activatedRoute: ActivatedRouteStub = new ActivatedRouteStub({
    id: chrisId,
  });
  let dialog: MatDialog;
  let dialogSpy: jasmine.Spy;
 // const teams = [{ _id: 'team1' }, { _id: 'team2' }];
 // const submissions = [{ _id: 'submission1', taskId: 'task1' }, { _id: 'submission2', taskId: 'task2' }];
 // const photo = 'photo1';
  const team = { _id: 'teamId1', teamName: 'team1', startedHuntId: 'some_started_hunt_id' };
  // const mockSubmissionService = {
  //   getSubmissionsByTeam: (teamId: string) => of([
  //     { _id: 'submission1_id', taskId: 'task1_id' },
  //     { _id: 'submission2_id', taskId: 'task2_id' },
  //   ]),
  //   getPhotoFromSubmission: (submissionId: string) => of('photo_url')
  // };

  // const mockTeamService = {
  //   getAllStartedHuntTeams: (startedHuntuntId: string) => of([
  //     { _id: 'team1_id', teamName: 'Team 1' },
  //     { _id: 'team2_id', teamName: 'Team 2' },
  //   ])
  // };

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [
        HttpClientModule,
        RouterTestingModule,
        MatCardModule,
        EndedHuntDetailsComponent,
        EndedHuntCardComponent,
        HttpClientModule,
        HttpClientTestingModule,
      ],
      providers: [
        { provide: HostService, useValue: mockHostService },
        { provide: StartedHuntService, useValue: mockStartedHuntService},
        { provide: ActivatedRoute, useValue: activatedRoute },
        { provide: MatDialog, useValue: { open: jasmine.createSpy('open') } },
       // { provide: SubmissionService, useValue: mockSubmissionService },
        //{ provide: TeamService, useValue: mockTeamService },
      ],
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(EndedHuntDetailsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
    dialog = TestBed.inject(MatDialog);
    dialogSpy = dialog.open as jasmine.Spy;
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should get task name', () => {
    const mockStartedHunt: StartedHunt = {
      _id: 'some_id',
      accessCode: 'some_access_code',
      completeHunt: {
        hunt: {
          _id: 'hunt_id',
          hostId: 'host_id',
          name: 'hunt_name',
          description: 'hunt_description',
          est: 20,
          numberOfTasks: 4
        },
        tasks: [
          {
            _id: 'task1', name: 'Task 1',
            huntId: '',
            status: false,
            photos: []
          },
          {
            _id: 'task2', name: 'Task 2',
            huntId: '',
            status: false,
            photos: []
          },
        ],
      },
        submissionIds: [ 'submission1', 'submission2' ],
    };
    component.startedHunt = mockStartedHunt;
    expect(component.getTaskName('task1')).toEqual('Task 1');
    expect(component.getTaskName('task2')).toEqual('Task 2');
  });

  it('should open dialog', () => {
    const mockPhoto = 'photo1';
    const mockPhotos = ['photo1', 'photo2', 'photo3'];
    component.openDialog(mockPhoto, mockPhotos);
    expect(dialogSpy).toHaveBeenCalledWith(PhotoDialogComponent, {
      data: {
        currentPhoto: mockPhoto,
        photos: mockPhotos,
      },
    });
  });

  it('should call getAllStartedHuntTeams with the correct teamId', () => {
    const teamId = 'team1_id';
    const teamService = TestBed.inject(TeamService);
    const spy = spyOn(teamService, 'getAllStartedHuntTeams').and.callThrough();

    // Initialize startedHunt before calling getTeamName
    component.startedHunt = MockStartedHuntService.testStartedHunts[0];

    component.getTeamName(teamId);

    expect(spy).toHaveBeenCalledWith(component.startedHunt._id);
  });

  it('should call getPhotoFromSubmission with the correct submissionId', () => {
    const submission: Submission = {
      _id: 'submission1_id',
      taskId: 'task1_id',
      teamId: 'team1_id',
      photoPath: 'path/to/photo.jpg'
    };
    const submissionService = TestBed.inject(SubmissionService);
    const spy = spyOn(submissionService, 'getPhotoFromSubmission').and.callThrough();

    component.fetchPhoto(submission, team);

    expect(spy).toHaveBeenCalledWith(submission._id);
  });
});
