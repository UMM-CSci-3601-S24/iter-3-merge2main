import { Component, OnDestroy, OnInit } from "@angular/core";
import { FormsModule } from "@angular/forms";
import { MatButtonModule } from "@angular/material/button";
import { MatCardModule } from "@angular/material/card";
import { MatOptionModule } from "@angular/material/core";
import { MatFormFieldModule } from "@angular/material/form-field";
import { MatIconModule } from "@angular/material/icon";
import { MatInputModule } from "@angular/material/input";
import { MatListModule } from "@angular/material/list";
import { MatRadioModule } from "@angular/material/radio";
import { MatSelectModule } from "@angular/material/select";
import { MatTooltipModule } from "@angular/material/tooltip";
import { RouterLink } from "@angular/router";
import { Hunt } from "../hunts/hunt";
import { Subject, takeUntil } from "rxjs";
import { MatSnackBar } from "@angular/material/snack-bar";
import { HostService } from "./host.service";
import { HuntCardComponent } from "../hunts/hunt-card.component";
import { StartedHunt } from "../startHunt/startedHunt";
import { EndedHuntCardComponent } from "../endedHunts/ended-hunt-card.component";
import { StartedHuntService } from "../startHunt/startedHunt.service";

@Component({
  selector: 'app-host-profile-component',
  templateUrl: 'host-profile.component.html',
  styleUrls: ['./host-profile.component.scss'],
  providers: [],
  standalone: true,
  imports: [EndedHuntCardComponent,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    FormsModule,
    MatSelectModule,
    MatOptionModule,
    MatRadioModule,
    MatListModule,
    RouterLink,
    MatButtonModule,
    MatTooltipModule,
    MatIconModule,
    HuntCardComponent]
})

export class HostProfileComponent implements OnInit, OnDestroy {
  public serverHunts: Hunt[];
  public userName: string;
  public name: string;
  public hostId: "588945f57546a2daea44de7c";
  public viewType: 'card'
  public serverEndedHunts: StartedHunt[];

  errMsg = '';
  private ngUnsubscribe = new Subject<void>();

  constructor(private hostService: HostService,
    private snackBar: MatSnackBar,
    private startedHuntService: StartedHuntService) {
  }

  getHuntsFromServer(): void {
    this.hostService.getHunts(this.hostId).pipe(
      takeUntil(this.ngUnsubscribe)
    ).subscribe({
      next: (returnedHunts) => {
        this.serverHunts = returnedHunts;
      },
      error: (err) => {
        if (err.error instanceof ErrorEvent) {
          this.errMsg = `Problem in the client – Error: ${err.error.message}`;
        } else {
          this.errMsg = `Problem contacting the server – Error Code: ${err.status}\nMessage: ${err.message}`;
        }
        this.snackBar.open(
          this.errMsg,
          'OK',
          { duration: 6000 });
      },
    });
  }

  getEndedHunts(): void {
    this.startedHuntService.getEndedHunts().pipe(
      takeUntil(this.ngUnsubscribe)
    ).subscribe({
      next: (returnedEndedHunts) => {
        this.serverEndedHunts = returnedEndedHunts;
      },
      error: (err) => {
        if (err.error instanceof ErrorEvent) {
          this.errMsg = `Problem in the client – Error: ${err.error.message}`;
        } else {
          this.errMsg = `Problem contacting the server – Error Code: ${err.status}\nMessage: ${err.message}`;
        }
        this.snackBar.open(
          this.errMsg,
          'OK',
          { duration: 6000 });
      },
    });
  }

  onHuntDeleted(deletedHuntId: string) {
    this.serverEndedHunts = this.serverEndedHunts.filter(hunt => hunt._id !== deletedHuntId);
  }

  ngOnInit(): void {
    this.getHuntsFromServer();
    this.getEndedHunts();
  }

  ngOnDestroy() {
    this.ngUnsubscribe.next();
    this.ngUnsubscribe.complete();
  }

}
