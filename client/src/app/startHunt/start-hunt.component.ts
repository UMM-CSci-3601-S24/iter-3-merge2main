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


@Component({
  selector: 'app-start-hunt-component',
  templateUrl: 'start-hunt.component.html',
  styleUrls: ['./start-hunt.component.scss'],
  providers: [],
  standalone: true,
  imports: [MatCard, MatCardContent, MatCardActions, MatIconModule, CommonModule, MatProgressBarModule]
})

export class StartHuntComponent implements OnInit, OnDestroy {
  startedHunt: StartedHunt;
  huntBegun = false;
  error: { help: string, httpResponse: string, message: string };

  private ngUnsubscribe = new Subject<void>();

  constructor(private snackBar: MatSnackBar,
    private route: ActivatedRoute,
    private hostService: HostService,
    private router: Router,
    public dialog: MatDialog,
    private startedHuntService: StartedHuntService) { }

  ngOnInit(): void {

    this.route.paramMap.pipe(

      map((paramMap: ParamMap) => paramMap.get('accessCode')),

      switchMap((accessCode: string) => this.startedHuntService.getStartedHunt(accessCode)),

      takeUntil(this.ngUnsubscribe)
    ).subscribe({
      next: startedHunt => {
        this.startedHunt = startedHunt;
        console.log(this.startedHunt);
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
}
