import { HttpClient } from "@angular/common/http";
import { HttpTestingController, HttpClientTestingModule } from "@angular/common/http/testing";
import { TestBed } from "@angular/core/testing";
import { Submission } from "./submission";
import { SubmissionService } from "./submission.service";
import { StartedHunt } from "../startHunt/startedHunt";
import { Hunt } from "../hunts/hunt";

describe('SubmissionService', () => {
  const testSubmissions: Submission[] = [
    {
      _id: '1234',
      taskId: '5678',
      teamId: '8765',
      photoPath: 'photo1.jpg',
      submitTime: new Date('2021-01-01T00:00:00Z'),
    },
    {
      _id: '5432',
      taskId: '8765',
      teamId: '5678',
      photoPath: 'photo2.jpg',
      submitTime: new Date('2021-01-02T00:00:00Z'),
    },
    {
      _id: '9876',
      taskId: '8765', // Different task than Submission 1 from teamId 8765
      teamId: '8765', // Same teamId as Team 2
      photoPath: 'photo3.jpg',
      submitTime: new Date('2021-01-03T00:00:00Z'),
    }
  ];

  const testHunts: Hunt[] = [
    {
      _id: 'hunt1_id',
      hostId: 'ann_hid',
      name: 'Anns Hunt',
      description: 'exciting hunt',
      est: 30,
      numberOfTasks: 3,
    },
    {
      _id: 'hunt2_id',
      hostId: 'fran_hid',
      name: 'Frans Hunt',
      description: 'super exciting hunt',
      est: 45,
      numberOfTasks: 2,
    },
  ];

  const testStartedHunts: StartedHunt[] = [
    {
      _id: 'startedHunt1_id',
      accessCode: '123456',
      completeHunt: {
        hunt: testHunts[0],
        tasks: [
          {
            _id: '5678',
            huntId: 'ann_id',
            name: 'Take a picture of a bird',
            status: false,
            photos: [],
          },
          {
            _id: '8765',
            huntId: 'ann_id',
            name: 'Take a picture of a dog',
            status: false,
            photos: [],
          },
          {
            _id: '1357',
            huntId: 'ann_id',
            name: 'Take a picture of a Stop sign',
            status: false,
            photos: [],
          },
        ],
      },
      submissionIds: ['1234', '5432'],
    },
    {
      _id: 'startedHunt2_id',
      accessCode: '654321',
      completeHunt: {
        hunt: testHunts[1],
        tasks: [
          {
            _id: '4545',
            huntId: 'hunt2_id',
            name: 'Take a picture of a restaurant',
            status: false,
            photos: [],
          },
          {
            _id: '5656',
            huntId: 'hunt2_id',
            name: 'Take a picture of a cat',
            status: false,
            photos: [],
          },
        ],
      },
      submissionIds: ['9876'],
    },
  ];

  let submissionService: SubmissionService;
  let httpClient: HttpClient;
  let httpTestingController: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule]
    });

    httpClient = TestBed.inject(HttpClient);
    httpTestingController = TestBed.inject(HttpTestingController);
    submissionService = new SubmissionService(httpClient);
  });

  afterEach(() => {
    httpTestingController.verify();
  });

  it('should be created', () => {
    expect(submissionService).toBeTruthy();
  });

  it('should get submission by id', () => {
    const submissionId = '1234';
    const submission = testSubmissions.find(s => s._id === submissionId);
    submissionService.getSubmission(submissionId).subscribe(sub => {
      expect(sub).toEqual(submission);
    });

    const req = httpTestingController.expectOne(`${submissionService.submissionUrl}/${submissionId}`);
    expect(req.request.method).toEqual('GET');
    req.flush(submission);
  });

  it('should get all submissions by team', () => {
    const teamId = '8765';
    const teamSubmissions = testSubmissions.filter(s => s.teamId === teamId);
    submissionService.getSubmissionsByTeam(teamId).subscribe(submissions => {
      expect(submissions).toEqual(teamSubmissions);
    });

    const req = httpTestingController.expectOne(`${submissionService.submissionUrl}/team/${teamId}`);
    expect(req.request.method).toEqual('GET');
    req.flush(teamSubmissions);
  });

  it('should get all submissions by task', () => {
    const taskId = '8765';
    const taskSubmissions = testSubmissions.filter(s => s.taskId === taskId);
    submissionService.getSubmissionsByTask(taskId).subscribe(submissions => {
      expect(submissions).toEqual(taskSubmissions);
    });

    const req = httpTestingController.expectOne(`${submissionService.submissionUrl}/task/${taskId}`);
    expect(req.request.method).toEqual('GET');
    req.flush(taskSubmissions);
  });

  it('should get all submissions by team and task', () => {
    const teamId = '8765';
    const taskId = '8765';
    const teamTaskSubmissions = testSubmissions.filter(s => s.teamId === teamId && s.taskId === taskId);
    submissionService.getSubmissionsByTeamAndTask(teamId, taskId).subscribe(submissions => {
      expect(submissions).toEqual(teamTaskSubmissions);
    });

    const req = httpTestingController.expectOne(`${submissionService.submissionUrl}/team/${teamId}/task/${taskId}`);
    expect(req.request.method).toEqual('GET');
    req.flush(teamTaskSubmissions);
  });

  it('should get all submissions by started hunt', () => {
    const startedHuntId = 'startedHunt1_id';
    const startedHuntSubmissions = testSubmissions.filter(s => testStartedHunts.find(sh => sh.submissionIds.includes(s._id)));
    submissionService.getSubmissionsByStartedHunt(startedHuntId).subscribe(submissions => {
      expect(submissions).toEqual(startedHuntSubmissions);
    });

    const req = httpTestingController.expectOne(`${submissionService.submissionUrl}/startedHunt/${startedHuntId}`);
    expect(req.request.method).toEqual('GET');
    req.flush(startedHuntSubmissions);
  });

  it('should get photo from submission', () => {
    const submissionId = '1234';
    const photoPath = 'photo1.jpg';
    submissionService.getPhotoFromSubmission(submissionId).subscribe(photo => {
      expect(photo).toEqual(photoPath);
    });

    const req = httpTestingController.expectOne(`${submissionService.submissionUrl}/${submissionId}/photo`);
    expect(req.request.method).toEqual('GET');
    req.flush(photoPath);
  });
});
