import { Component } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';

@Component({
  selector: 'app-hunter-ended-hunt',
  standalone: true,
  imports: [RouterLink, MatButtonModule, MatCardModule],
  templateUrl: './hunter-ended-hunt.component.html',
  styleUrl: './hunter-ended-hunt.component.scss'
})
export class HunterEndedHuntComponent {
  constructor(
    private router: Router,
  ) { }
}
