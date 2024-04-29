import { HttpClientModule } from '@angular/common/http';
import { Component, OnDestroy, OnInit } from '@angular/core';
import { MatIconButton } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatDivider } from '@angular/material/divider';
import { MatIcon } from '@angular/material/icon';
import { AddTaskComponent } from 'src/app/hunts/addTask/add-task.component';
import { EndedHuntCardComponent } from '../ended-hunt-card.component';
import { Observable, Subject, map, switchMap, takeUntil } from 'rxjs';
import { MatDialog } from '@angular/material/dialog';
import { ActivatedRoute, ParamMap, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { PhotoDialogComponent } from './photo-dialog/photo-dialog.component';
import { StartedHunt } from 'src/app/startHunt/startedHunt';
import { StartedHuntService } from 'src/app/startHunt/startedHunt.service';
import { Submission } from 'src/app/submissions/submission';
import { SubmissionService } from 'src/app/submissions/submission.service';
import { TeamService } from 'src/app/teams/team.service';
import { Team } from 'src/app/teams/team';

@Component({
  selector: 'app-ended-hunt-details',
  templateUrl: './ended-hunt-details.component.html',
  styleUrls: ['./ended-hunt-details.component.scss'],
  standalone: true,
  imports: [
    EndedHuntCardComponent,
    MatCardModule,
    AddTaskComponent,
    MatDivider,
    MatIconButton,
    MatIcon,
    HttpClientModule,
    CommonModule,
    RouterLink
  ],
})
export class EndedHuntDetailsComponent implements OnInit, OnDestroy {
  confirmDeleteHunt: boolean = false;
  startedHunt: StartedHunt;
  submissions: Submission[];
  teams: Team[];
  team: Team;
  error: { help: string; httpResponse: string; message: string };

  private ngUnsubscribe = new Subject<void>();

  constructor(
    private route: ActivatedRoute,
    private startedHuntService: StartedHuntService,
    private submissionService: SubmissionService,
    private teamService: TeamService,
    public dialog: MatDialog,

  ) { }

  taskSubmissions: { [taskId: string]: { teamName: string, photo: string }[] } = {};

  ngOnInit(): void {
    console.log('ngOnInit started');
    this.route.paramMap.pipe(
      map((paramMap: ParamMap) => {
        console.log('Getting id from paramMap:', paramMap.get('id'));
        return paramMap.get('id');
      }),
      switchMap((id: string) => {
        console.log('Fetching ended hunt by id:', id);
        return this.startedHuntService.getStartedHuntById(id);
      }),
      takeUntil(this.ngUnsubscribe)
    ).subscribe({
      next: (startedHunt) => {
        console.log('Received started hunt:', startedHunt);
        this.startedHunt = startedHunt;
        if (this.startedHunt) {
          this.loadTaskSubmissions();
        } else {
          console.error('Started hunt is null');
        }
      },
      error: (err) => {
        console.error('Error occurred while fetching started hunt:', err);
      }
    });
  }

  loadTaskSubmissions(): void {
    console.log('loadTaskSubmissions started');
    this.fetchTeams().subscribe({
      next: (teams) => {
        console.log('Received teams:', teams);
        teams.forEach(team => this.fetchSubmissions(team));
      },
      error: (err) => {
        console.error('Error occurred while fetching teams:', err);
      }
    });
  }

  fetchTeams(): Observable<Team[]> {
    return this.teamService.getAllStartedHuntTeams(this.startedHunt._id);
  }

  fetchSubmissions(team: Team): void {
    console.log('Fetching submissions by team:', team._id);
    this.submissionService.getSubmissionsByTeam(team._id).subscribe({
      next: (submissions) => {
        console.log('Received submissions:', submissions);
        this.submissions = submissions; // Use the submissions array
        submissions.forEach(submission => this.fetchPhoto(submission, team));
      },
      error: (err) => {
        console.error('Error occurred while fetching submissions:', err);
      }
    });
  }

  fetchPhoto(submission: Submission, team: Team): void {
    if (!this.taskSubmissions[submission.taskId]) {
      this.taskSubmissions[submission.taskId] = [];
    }
    console.log('Fetching photo from submission:', submission._id);
    this.submissionService.getPhotoFromSubmission(submission._id).subscribe(photoBase64 => {
      const photo = this.decodeImage(photoBase64);
      console.log('Received photo:', photo);
      this.taskSubmissions[submission.taskId].push({
        teamName: team.teamName,
        photo: photo,
      });
      // Find the corresponding task and push the photo to its photos array
      const task = this.startedHunt.completeHunt.tasks.find(task => task._id === submission.taskId);
      if (task) {
        if (!task.photos) {
          task.photos = [];
        }
        task.photos.push(photo);
      }
    });
  }

  //Decode photo from base64 to display it
  decodeImage(image: string): string {
    return `data:image/jpeg;base64,${image}`;
  }

  getTeamName(teamId: string): void {
    this.teamService.getAllStartedHuntTeams(this.startedHunt._id).subscribe({
      next: (teams) => {
        const team = teams.find(team => team._id === teamId);
        if (team) {
          return team.teamName;
        } else {
          console.error(`Team with id ${teamId} not found.`);
        }
      },
      error: (err) => {
        console.error(err);
      }
    });
  }

  getTaskName(taskId: string): string {
    return this.startedHunt.completeHunt.tasks.find(
      (task) => task._id === taskId
    ).name;
  }

  ngOnDestroy(): void {
    this.ngUnsubscribe.next();
    this.ngUnsubscribe.complete();
  }

  openDialog(photo: string, photos: string[]): void {
    this.dialog.open(PhotoDialogComponent, {
      data: {
        currentPhoto: photo,
        photos: photos
      }
    });
  }
}
