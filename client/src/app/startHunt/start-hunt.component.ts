import { Component, OnDestroy, OnInit } from "@angular/core";
import { MatDialog } from "@angular/material/dialog";
import { MatSnackBar } from "@angular/material/snack-bar";
import { ActivatedRoute, Router, ParamMap } from "@angular/router";
import { Subject, Subscription, map, switchMap, takeUntil } from "rxjs";
import { HostService } from "../hosts/host.service";
import { StartedHunt } from "./startedHunt";
import { MatCard, MatCardActions, MatCardContent, MatCardTitle } from "@angular/material/card";
import { MatIconModule } from '@angular/material/icon';
import { CommonModule } from '@angular/common';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { WebSocketService } from '../web-socket.service';
//import { Task } from "../hunts/task";

@Component({
  selector: 'app-start-hunt-component',
  templateUrl: 'start-hunt.component.html',
  styleUrls: ['./start-hunt.component.scss'],
  providers: [],
  standalone: true,
  imports: [MatCard, MatCardContent, MatCardActions, MatIconModule, CommonModule, MatProgressBarModule, MatCardTitle,]
})

export class StartHuntComponent implements OnInit, OnDestroy {
  startedHunt: StartedHunt;
  huntBegun = false;
  error: { help: string, httpResponse: string, message: string };

  private ngUnsubscribe = new Subject<void>();

  private photoUploadSubscription: Subscription;

  constructor(private snackBar: MatSnackBar, private route: ActivatedRoute, private hostService: HostService, private router: Router, public dialog: MatDialog, private webSocketService: WebSocketService) { }

  ngOnInit(): void {
    this.photoUploadSubscription = this.webSocketService.onMessage()
    .subscribe((message: StartedHunt) => {
      console.log('Received message from websocket: ' + JSON.stringify(message));
      this.snackBar.open(message['photo-uploaded'] + ' uploaded', 'OK', { duration: 3000 });
      this.updatePhotos();
    });

    this.route.paramMap.pipe(

      map((paramMap: ParamMap) => paramMap.get('accessCode')),

      switchMap((accessCode: string) => this.hostService.getStartedHunt(accessCode)),

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

  updatePhotos() {
    setTimeout(() => {
      this.hostService.getStartedHunt(this.startedHunt.accessCode)
      .subscribe({
        next: startedHunt => {
          this.startedHunt = startedHunt;
        },
        error: _err => {
          this.error = {
            help: 'There was a problem updating the hunt – try again.',
            httpResponse: _err.message,
            message: _err.error?.title,
          };
        }
      });
    }, 500);
  }

  getTaskName(taskId: string): string {
    return this.startedHunt.completeHunt.tasks.find(
      (task) => task._id === taskId
    ).name;
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
    this.photoUploadSubscription.unsubscribe();
  }

  endHunt(): void {
    this.hostService.endStartedHunt(this.startedHunt._id)
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
