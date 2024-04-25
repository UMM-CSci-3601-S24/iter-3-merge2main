import { HttpClient } from "@angular/common/http";
import { HttpTestingController, HttpClientTestingModule } from "@angular/common/http/testing";
import { TestBed } from "@angular/core/testing";
import { Submission } from "./submission";
import { SubmissionService } from "./submission.service";

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


});
