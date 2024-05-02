import { Component, OnDestroy, OnInit } from "@angular/core";
import { MatDialog } from "@angular/material/dialog";
import { MatSnackBar } from "@angular/material/snack-bar";
import { ActivatedRoute, Router, ParamMap } from "@angular/router";
import { Subject, map, switchMap, takeUntil } from "rxjs";
import { HostService } from "../hosts/host.service";
import { StartedHunt } from "./startedHunt";
import { MatCard, MatCardActions, MatCardContent } from "@angular/material/card";
import { MatIconModule } from '@angular/material/icon';
import { CommonModule } from '@angular/common';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { StartedHuntService } from "./startedHunt.service";
import { SubmissionService } from "../submissions/submission.service";
import { Submission } from "../submissions/submission";
import { SubmissionCardComponent } from "../submissions/submission-card/submission-card.component";
import { MatFormFieldModule } from "@angular/material/form-field";
import { TeamService } from "../teams/team.service";
import { Team } from "../teams/team"; // Import the 'Team' class from the appropriate module

@Component({
  selector: 'app-start-hunt-component',
  templateUrl: 'start-hunt.component.html',
  styleUrls: ['./start-hunt.component.scss'],
  providers: [],
  standalone: true,
  imports: [
    MatCard,
    MatCardContent,
    MatCardActions,
    MatIconModule,
    CommonModule,
    MatProgressBarModule,
    SubmissionCardComponent,
    MatFormFieldModule]
})

export class StartHuntComponent implements OnInit, OnDestroy {
  startedHunt: StartedHunt;
  huntBegun = false;
  error: { help: string, httpResponse: string, message: string };
  public serverSubmissions: Submission[];

  private ngUnsubscribe = new Subject<void>();

  teams: Team[];

  constructor(private snackBar: MatSnackBar,
    private route: ActivatedRoute,
    private hostService: HostService,
    private router: Router,
    public dialog: MatDialog,
    private startedHuntService: StartedHuntService,
    private submissionService: SubmissionService,
    private teamService: TeamService
  ) { }

  ngOnInit(): void {
    this.getHunt();
    this.getTeams();

    setInterval(() => {
      this.getSubmissionsFromServer();
    }, 2000);
  }

  getTeams(): void {
    this.teamService.getTeams().subscribe(teams => {
      const huntTeams = teams.filter(team => team.startedHuntId === this.startedHunt._id);
      const highestTeamName = Math.max(...huntTeams.map(team => {
        const lastChar = team.teamName[team.teamName.length - 1];
        const parsed = parseInt(lastChar);
        return isNaN(parsed) ? 0 : parsed;
      }));
      this.teams = new Array(highestTeamName).fill({});
    });
  }

    getHunt(): void {
      this.route.paramMap.pipe(
        map((paramMap: ParamMap) => paramMap.get('accessCode')),
        switchMap((accessCode: string) => this.startedHuntService.getStartedHunt(accessCode)),
        takeUntil(this.ngUnsubscribe)
      ).subscribe({
        next: startedHunt => {
          this.startedHunt = startedHunt;
          console.log(this.startedHunt);
          this.getSubmissionsFromServer();
          return ;
        },
        error: _err => {
          this.error = {
            help: 'There was a problem starting the hunt – try again.',
            httpResponse: _err.message,
            message: _err.error?.title,
          };
        }
      });
    }

  beginHunt() {
    this.huntBegun = true;
  }

  onEndHuntClick(event: Event) {
    event.stopPropagation();
    if (window.confirm('Are you sure you want to end this hunt?')) {
      this.endHunt()
    }
  }

  ngOnDestroy() {
    this.ngUnsubscribe.next();
    this.ngUnsubscribe.complete();

  }

  endHunt(): void {
    this.startedHuntService.endStartedHunt(this.startedHunt._id)
      .subscribe({
        next: () => {
          this.snackBar.open('Hunt ended successfully', 'Close', {
            duration: 2000,
          });
          this.router.navigate(['/endedHunts', this.startedHunt._id]); // Navigate to ended hunt details page after ending the hunt
        },
        error: _err => {
          this.error = {
            help: 'There was a problem ending the hunt – try again.',
            httpResponse: _err.message,
            message: _err.error?.title,
          };
        }
      });
  }

  getSubmissionsFromServer(): void {
    console.log('Started Hunt ID:', this.startedHunt._id);
    this.submissionService.getSubmissionsByStartedHunt(this.startedHunt._id).pipe(
      takeUntil(this.ngUnsubscribe)
    ).subscribe({
      next: (returnedSubmissions: Submission[]) => {
        this.serverSubmissions = returnedSubmissions;
      },
      error: (err) => {
        if (err.error instanceof ErrorEvent) {
          this.error = {
            help: `Problem in the client – Error: ${err.error.message}`,
            httpResponse: '',
            message: '',
          };
        } else {
          this.error = {
            help: `Problem contacting the server – Error Code: ${err.status}\nMessage: ${err.message}`,
            httpResponse: '',
            message: '',
          };
        }
        this.snackBar.open(
          this.error.help,
          'OK',
          { duration: 6000 });
      },
    });
  }
}
