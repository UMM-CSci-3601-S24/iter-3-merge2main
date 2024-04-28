import { CommonModule } from "@angular/common";
import { Component, EventEmitter, Output, input } from "@angular/core";
import { MatButtonModule } from "@angular/material/button";
import { MatCardModule } from "@angular/material/card";
import { MatIconModule } from "@angular/material/icon";
import { MatListModule } from "@angular/material/list";
import { RouterLink, Router } from "@angular/router";
import { HostService } from "../hosts/host.service";
import { StartedHunt } from "../startHunt/startedHunt";
import { StartedHuntService } from "../startHunt/startedHunt.service";

@Component({
  selector: 'app-ended-hunt-card',
  templateUrl: './ended-hunt-card.component.html',
  styleUrls: ['./ended-hunt-card.component.scss'],
  standalone: true,
  imports: [CommonModule, MatCardModule, MatButtonModule, MatListModule, MatIconModule, RouterLink]
})
export class EndedHuntCardComponent {
  startedHunt = input.required<StartedHunt>();
  simple = input(true);
  customConfirm = 'Are you sure you want to delete this hunt?';

  constructor(private hostService: HostService,
    private router: Router,
    private startedHuntService: StartedHuntService) {}

  @Output() readonly huntDeleted = new EventEmitter<string>();

  deleteEndedHunt(id: string): void {
    if (window.confirm(this.customConfirm)) {
      this.startedHuntService.deleteStartedHunt(id).subscribe(() => {
        this.huntDeleted.emit(id);
      });
    }
  }
}
