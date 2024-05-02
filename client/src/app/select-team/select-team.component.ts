import { FormsModule } from '@angular/forms';
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { StartedHuntService } from '../startHunt/startedHunt.service';
import { TeamService } from '../teams/team.service';
import { Team } from '../teams/team';
import { CommonModule } from '@angular/common';
import { MatError } from '@angular/material/form-field';

@Component({
  selector: 'app-select-team',
  standalone: true,
  imports: [MatError, CommonModule, FormsModule],
  templateUrl: './select-team.component.html',
  styleUrl: './select-team.component.scss'
})
export class SelectTeamComponent implements OnInit {
  teams: Team[];
  startedHuntId: string;
  accessCode: string;

  constructor(private route: ActivatedRoute,
    private startedHuntService: StartedHuntService,
    private teamService: TeamService,
    private router: Router) { }

    ngOnInit(): void {
      console.log('ngOnInit called');
      this.route.params.subscribe(params => {
        console.log('Params:', params);
        this.accessCode = params['accessCode'];
        this.startedHuntService.getStartedHunt(this.accessCode).subscribe(startedHunt => {
          console.log('Started Hunt:', startedHunt);
          this.startedHuntId = startedHunt._id;
          this.teamService.getAllStartedHuntTeams(this.startedHuntId).subscribe(teams => {
            console.log('Teams:', teams);
            this.teams = teams.map(team => ({ ...team, selected: false }));
          });
        });
      });
    }


  toggleSelection(team: Team): void {
    this.teams.forEach(t => {
      if (t !== team) {
        t.selected = false;
      }
    });
    team.selected = !team.selected;
  }

  isTeamSelected(): boolean {
    return this.teams && this.teams.some(team => team.selected);
  }

  proceed(): void {
    const selectedTeam = this.teams.find(team => team.selected);
    if (selectedTeam) {
      const selectedTeamId = selectedTeam._id;
      this.router.navigate(['/hunter-view', this.accessCode, 'teams', selectedTeamId]);
    }
  }

  trackById(index: number, item: Team): string {
    return item._id;
  }

}
