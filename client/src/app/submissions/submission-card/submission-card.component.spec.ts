import { TestBed } from '@angular/core/testing';
import { SubmissionCardComponent } from './submission-card.component';
import { SubmissionService } from '../submission.service';
import { TeamService } from 'src/app/teams/team.service';
import { of } from 'rxjs';
import { Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';

describe('SubmissionCardComponent', () => {
  let component: SubmissionCardComponent;
  let submissionService: SubmissionService;
  let teamService: TeamService;
  let httpClient: HttpClient;
  let router: Router;

  beforeEach(() => {
    submissionService = jasmine.createSpyObj('SubmissionService', ['getSubmission', 'deleteSubmission']);
    teamService = jasmine.createSpyObj('TeamService', ['getTeam']);

    TestBed.configureTestingModule({
      providers: [
        SubmissionCardComponent,
        { provide: SubmissionService, useValue: submissionService },
        { provide: TeamService, useValue: teamService },
        { provide: Router, useValue: router},
        { provide: HttpClient, useValue: httpClient}
      ]
    });

    component = TestBed.inject(SubmissionCardComponent);
  });

  it('should call submissionService.deleteSubmission and submissionDeleted.emit when deleteSubmission is called', () => {
    const id = 'testId';
    (submissionService.deleteSubmission as jasmine.Spy).and.returnValue(of(undefined));

    spyOn(window, 'confirm').and.returnValue(true);

    component.deleteSubmission(id);

    expect(submissionService.deleteSubmission).toHaveBeenCalledWith(id);

  });
});
