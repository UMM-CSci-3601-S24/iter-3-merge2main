import { CommonModule } from '@angular/common';
import { Component, Input, input } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatListModule } from '@angular/material/list';
import { Router, RouterLink } from '@angular/router';
import { Submission } from '../submission';
import { HostService } from 'src/app/hosts/host.service';
import { StartedHuntService } from 'src/app/startHunt/startedHunt.service';
import { TeamService } from 'src/app/teams/team.service';
import { SubmissionService } from '../submission.service';
import { StartedHunt } from 'src/app/startHunt/startedHunt';
import { Observable, map } from 'rxjs';

@Component({
  selector: 'app-submission-card',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatButtonModule,
    MatListModule,
    MatIconModule,
    RouterLink],
  templateUrl: './submission-card.component.html',
  styleUrl: './submission-card.component.scss'
})
export class SubmissionCardComponent {

  submission = input.required<Submission>();
  simple = input(true);

  @Input() startedHunt: StartedHunt;
  taskId: string;
  teamId: string;

  @Input() context: 'start-hunt';

  constructor(
    private submissionService: SubmissionService,
    private startedHuntService: StartedHuntService,
    private hostService: HostService,
    private teamService: TeamService,
    private router: Router
  ) { }

  // this retrieves the task description from the started hunt
  getTaskDescription(taskId: string): string {
    const task = this.startedHunt.completeHunt.tasks.find(task => task._id === taskId);
    return task ? task.name : '';
  }

  // this retrieves the team name via the teamId in the submission
  // If the team name call fails, it will return an empty string
  getTeamName(teamId: string): string {
    let teamName = '';
    this.teamService.getTeam(teamId).subscribe(
      team => teamName = team.teamName);
    return teamName;
  }

  // this retrieves the photo from the submission
  getPhoto(submissionId: string): Observable<string> {
    return this.submissionService.getPhotoFromSubmission(submissionId).pipe(
      map(photoBase64 => this.decodeImage(photoBase64))
    );
  }

  //Decode the image from base64 to display it
  decodeImage(image: string): string {
    return `data:image/jpeg;base64,${image}`;
  }

  // This deletes the submission
  deleteSubmission(submissionId: string): void {
    this.submissionService.deleteSubmission(submissionId).subscribe(() => {
      console.log('Submission deleted:', submissionId);
    });
  }

}
