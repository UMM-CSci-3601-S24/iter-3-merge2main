import { CommonModule } from '@angular/common';
import { Component, OnDestroy, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatSelect } from '@angular/material/select';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ActivatedRoute, Router } from '@angular/router';
import { StartedHunt } from 'src/app/startHunt/startedHunt';
import { Team } from '../team';
import { StartedHuntService } from 'src/app/startHunt/startedHunt.service';
import { TeamService } from '../team.service';
import { Subject } from 'rxjs';

@Component({
  selector: 'app-add-teams',
  standalone: true,
  imports: [MatSelect, FormsModule, CommonModule],
  templateUrl: './add-teams.component.html',
  styleUrl: './add-teams.component.scss'
})
export class AddTeamsComponent implements OnInit, OnDestroy{
  numTeams: number = 1;
  startedHunt: StartedHunt;
  teams: Team[] = [];
  accessCode: string;
  startedHuntId: string;

  private ngUnsubscribe: Subject<void> = new Subject<void>();

  constructor(
    private snackBar: MatSnackBar,
    private route: ActivatedRoute,
    private startedHuntService: StartedHuntService,
    private teamService: TeamService,
    private router: Router) {}

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      this.accessCode = params['accessCode'];
      this.startedHuntService.getStartedHunt(this.accessCode).subscribe(startedHunt => {
        this.startedHunt = startedHunt;
        this.startedHuntId = startedHunt._id;
      });
    });
  }

  ngOnDestroy(): void {

    this.snackBar.dismiss();
  }

  addTeams(id: string, numTeams: number): void {
    if (this.startedHunt) {
      this.teamService.addTeams(id, numTeams).subscribe(() => {
        this.snackBar.open('Teams added successfully', 'Close', {duration: 3000});
        this.router.navigate([`/startedHunts/${this.startedHunt.accessCode}`]);
      });
    } else {
      this.snackBar.open('Error adding teams', 'Close', {duration: 3000});
    }
  }

}
