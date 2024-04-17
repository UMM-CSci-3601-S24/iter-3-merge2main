import { Component, OnDestroy, OnInit } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ActivatedRoute, ParamMap, Router } from '@angular/router';
import { Subject } from 'rxjs';
import { map, switchMap, takeUntil } from 'rxjs/operators';
import { StartedHunt } from 'src/app/startHunt/startedHunt';
import { Task } from 'src/app/hunts/task';
import { HuntCardComponent } from 'src/app/hunts/hunt-card.component';
import { HostService } from 'src/app/hosts/host.service';
import { CommonModule } from '@angular/common';
import { MatDialog } from '@angular/material/dialog';
import { Ng2ImgMaxService } from 'ng2-img-max';


@Component({
  selector: 'app-hunter-view',
  standalone: true,
  imports: [HuntCardComponent, CommonModule, MatCardModule, MatIconModule],
  templateUrl: './hunter-view.component.html',
  styleUrl: './hunter-view.component.scss'
})
export class HunterViewComponent implements OnInit, OnDestroy {
  startedHunt: StartedHunt;
  tasks: Task[] = [];
  error: { help: string, httpResponse: string, message: string };
  imageUrls = {};

  private ngUnsubscribe = new Subject<void>();

  constructor(
    private hostService: HostService,
    private route: ActivatedRoute,
    private snackBar: MatSnackBar,
    private router: Router,
    public dialog: MatDialog,
    private ng2ImgMax: Ng2ImgMaxService
  ) { }

  ngOnInit(): void {
    this.route.paramMap.pipe(
      map((params: ParamMap) => params.get('accessCode')),
      switchMap((accessCode: string) => this.hostService.getStartedHunt(accessCode)),

      takeUntil(this.ngUnsubscribe)
      ).subscribe({
        next: startedHunt => {
          for (const task of startedHunt.completeHunt.tasks) {
            task.photos = [];
          }
          this.startedHunt = startedHunt;
          return;
        },
        error: _err => {
          this.error = {
            help: 'There is an error trying to load the tasks - Please try to run the hunt again',
            httpResponse: _err.message,
            message: _err.error?.title,
          };
        }
      });
  }

  ngOnDestroy(): void {
    this.ngUnsubscribe.next();
    this.ngUnsubscribe.complete();
  }

  onFileSelected(event, task: Task): void {
    let file: File = event.target.files[0];
    const fileType = file.type;
    if (fileType.match(/image\/*/)) {
      if (this.imageUrls[task._id] && !window.confirm('An image has already been uploaded for this task. Are you sure you want to replace it?')) {
        return;
      }

      if (file.size > .2 * 1024 * 1024) {
        console.log('compressing image');
        this.ng2ImgMax.compressImage(file, .2).subscribe(
          result => {
            file = result;  // replace the original file with the compressed one
            console.log('compressed image');
          },
          error => {
            console.error('Error compressing image', error);
            this.snackBar.open('Error compressing image. Please try again', 'Close', {
              duration: 3000
            });
          }
        );
      }

      const reader = new FileReader();
      reader.readAsDataURL(file);
      reader.onload = (event: ProgressEvent<FileReader>) => {
        this.imageUrls[task._id] = event.target.result.toString();
      };

      if (file) {
        if (task.photos.length > 0) {
          this.replacePhoto(file, task, this.startedHunt._id);
        }
        else {
          this.submitPhoto(file, task, this.startedHunt._id);
          console.log('photo submitted');
        }
      }
    }
  }

  submitPhoto(file: File, task: Task, startedHuntId: string): void {
    this.hostService.submitPhoto(startedHuntId, task._id, file).subscribe({
      next: (photoId: string) => {
        task.status = true;
        task.photos.push(photoId);
        this.snackBar.open('Photo uploaded successfully', 'Close', {
          duration: 3000
        });
      },
      error: (error: Error) => {
        console.error('Error uploading photo', error);
        this.snackBar.open('Error uploading photo. Please try again', 'Close', {
          duration: 3000
        });
      },
    });
  }

  replacePhoto(file: File, task: Task, startedHuntId: string): void {
    this.hostService.replacePhoto(startedHuntId, task._id, task.photos[0], file).subscribe({
      next: (photoId: string) => {
        task.photos[0] = photoId;
        this.snackBar.open('Photo replaced successfully', 'Close', {
          duration: 3000
        });
      },
      error: (error: Error) => {
        console.error('Error replacing photo', error);
        this.snackBar.open('Error replacing photo. Please try again', 'Close', {
          duration: 3000
        });
      },
    });
  }
}
