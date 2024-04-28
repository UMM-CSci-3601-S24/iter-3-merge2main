import { Component, input, Input} from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatListModule } from '@angular/material/list';
import { Router, RouterLink } from '@angular/router';
import { Hunt } from './hunt';
import { CommonModule } from '@angular/common';
import { HostService } from '../hosts/host.service';

@Component({
    selector: 'app-hunt-card',
    templateUrl: './hunt-card.component.html',
    styleUrls: ['./hunt-card.component.scss'],
    standalone: true,
    imports: [CommonModule, MatCardModule, MatButtonModule, MatListModule, MatIconModule, RouterLink]
})
export class HuntCardComponent {


  hunt = input.required<Hunt>();
  simple = input(true);
  @Input() context: 'host-profile' | 'hunt-profile' = 'hunt-profile';


  constructor(private hostService: HostService, private router: Router) {}

  startHunt(id: string): void {
    this.hostService.startHunt(id).subscribe((accessCode) => {
      this.router.navigate(['/startedHunts/', accessCode, 'addTeams']);
    });
  }
}


