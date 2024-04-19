import { HttpClientModule } from '@angular/common/http';
import { Component, OnDestroy, OnInit } from '@angular/core';
import { MatIconButton } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatDivider } from '@angular/material/divider';
import { MatIcon } from '@angular/material/icon';
import { AddTaskComponent } from 'src/app/hunts/addTask/add-task.component';
import { EndedHuntCardComponent } from '../ended-hunt-card.component';
import { EndedHunt } from '../endedHunt';
import { Subject, map, switchMap, takeUntil } from 'rxjs';
import { MatDialog } from '@angular/material/dialog';
import { ActivatedRoute, ParamMap, Router, RouterLink } from '@angular/router';
import { HostService } from 'src/app/hosts/host.service';
import { CommonModule } from '@angular/common';
import { PhotoDialogComponent } from './photo-dialog/photo-dialog.component';

@Component({
  selector: 'app-ended-hunt-details',
  templateUrl: './ended-hunt-details.component.html',
  styleUrls: ['./ended-hunt-details.component.scss'],
  standalone: true,
  imports: [
    EndedHuntCardComponent,
    MatCardModule,
    AddTaskComponent,
    MatDivider,
    MatIconButton,
    MatIcon,
    HttpClientModule,
    CommonModule,
    RouterLink
  ],
})
export class EndedHuntDetailsComponent implements OnInit, OnDestroy {
  confirmDeleteHunt: boolean = false;
  endedHunt: EndedHunt;
  error: { help: string; httpResponse: string; message: string };

  private ngUnsubscribe = new Subject<void>();

  constructor(
    private route: ActivatedRoute,
    private hostService: HostService,
    private router: Router,
    public dialog: MatDialog,

  ) {}

  ngOnInit(): void {
    this.route.paramMap
      .pipe(
        map((paramMap: ParamMap) => paramMap.get('id')),

        switchMap((id: string) => this.hostService.getEndedHuntById(id)),

        takeUntil(this.ngUnsubscribe)
      )
      .subscribe({
        next: (endedHunt) => {
          this.endedHunt = endedHunt;
          return;
        },
        error: (_err) => {
          this.error = {
            help: 'There was a problem loading the endedHunt – try again.',
            httpResponse: _err.message,
            message: _err.error?.title,
          };
        },
      });
  }

  getTaskName(taskId: string): string {
    return this.endedHunt.startedHunt.completeHunt.tasks.find(
      (task) => task._id === taskId
    )?.name;
  }

  ngOnDestroy(): void {
    this.ngUnsubscribe.next();
    this.ngUnsubscribe.complete();
  }

  openDialog(photo: string, photos: string[]): void {
    this.dialog.open(PhotoDialogComponent, {
      data: {
        currentPhoto: photo,
        photos: photos
      }
    });
  }
}
