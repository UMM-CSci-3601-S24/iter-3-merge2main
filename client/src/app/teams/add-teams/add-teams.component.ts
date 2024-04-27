import { CommonModule } from '@angular/common';
import { Component, OnDestroy, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatSelect } from '@angular/material/select';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ActivatedRoute, Router } from '@angular/router';
import { HostService } from 'src/app/hosts/host.service';
import { StartedHunt } from 'src/app/startHunt/startedHunt';
import { Team } from '../team';
import { StartedHuntService } from 'src/app/startHunt/startedHunt.service';

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

  constructor(
    private snackBar: MatSnackBar,
    private route: ActivatedRoute,
    private hostService: HostService,
    private startedHuntService: StartedHuntService,
    private router: Router) {}

  ngOnInit(): void {
    throw new Error('Method not implemented.');
  }
  ngOnDestroy(): void {
    throw new Error('Method not implemented.');
  }

}
