import { Component, OnDestroy, OnInit } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatButton } from '@angular/material/button';
import { ActivatedRoute, ParamMap, Router, RouterLink } from '@angular/router';
import { Subject, forkJoin } from 'rxjs';
import { map, switchMap, takeUntil } from 'rxjs/operators';
import { StartedHunt } from 'src/app/startHunt/startedHunt';
import { Task } from 'src/app/hunts/task';
import { HuntCardComponent } from 'src/app/hunts/hunt-card.component';
import { HostService } from 'src/app/hosts/host.service';
import { CommonModule } from '@angular/common';
import { MatDialog } from '@angular/material/dialog';
import { Ng2ImgMaxService } from 'ng2-img-max';
import { Team } from 'src/app/teams/team';
import { StartedHuntService } from 'src/app/startHunt/startedHunt.service';
import { SubmissionService } from 'src/app/submissions/submission.service';
import { Submission } from 'src/app/submissions/submission';


@Component({
  selector: 'app-hunter-view',
  standalone: true,
  imports: [HuntCardComponent, CommonModule, MatCardModule, MatIconModule, MatButton, RouterLink],
  templateUrl: './hunter-view.component.html',
  styleUrl: './hunter-view.component.scss'
})
export class HunterViewComponent implements OnInit, OnDestroy {
  startedHunt: StartedHunt;
  tasks: Task[] = [];
  error: { help: string, httpResponse: string, message: string };
  imageUrls = {};
  team: Team;
  teamId: string;
  submission: Submission;

  private ngUnsubscribe = new Subject<void>();

  constructor(
    private hostService: HostService,
    private startedHuntService: StartedHuntService,
    private submissionService: SubmissionService,
    private route: ActivatedRoute,
    private snackBar: MatSnackBar,
    private router: Router,
    public dialog: MatDialog,
    private ng2ImgMax: Ng2ImgMaxService

  ) { }

  ngOnInit(): void {
    this.route.paramMap.pipe(
      map((params: ParamMap) => {
        return {
          accessCode: params.get('accessCode'),
          teamId: params.get('teamId')
        };
      }),
      switchMap(({ accessCode, teamId }) => {
        this.teamId = teamId;
        return forkJoin({
          startedHunt: this.startedHuntService.getStartedHunt(accessCode),
          submissions: this.submissionService.getSubmissionsByTeam(teamId)
        });
      }),
      takeUntil(this.ngUnsubscribe)
    ).subscribe({
      next: ({ startedHunt, submissions }) => {
        this.startedHunt = startedHunt;
        this.loadPhotos(submissions);
      },
      error: _err => {
        let helpMessage = 'There is an error trying to load the tasks or the submissions - Please try to run the hunt again';
        const httpResponse = _err.message;
        let errorMessage = _err.error?.title;

        if (_err.status === 404) {
          helpMessage = 'The hunt you are trying to access does not exist. Please check the access code and try again.';
        } else if (_err.status === 500) {
          helpMessage = 'There is a server error. Please try again later.';
        }

        if (!_err.error?.title) {
          errorMessage = 'An unexpected error occurred';
        }

        this.error = {
          help: helpMessage,
          httpResponse: httpResponse,
          message: errorMessage,
        };
      }
    });
  }

  loadPhotos(submissions: Submission[]): void {
    if (submissions.length === 0) {
      return;
    }


    for (const submission of submissions) {
      const task = this.startedHunt.completeHunt.tasks.find(t => t._id === submission.taskId);
      if (task) {
        task.status = true;
        task.photos.push(submission.photoPath);
        this.submissionService.getPhotoFromSubmission(submission._id).subscribe({
          next: (photoBase64: string) => {
            this.imageUrls[task._id] = this.decodeImage(photoBase64);
          },
          error: (error: Error) => {
            console.error('Error loading photo', error);
            this.snackBar.open('Error loading photo. Please try again', 'Close', {
              duration: 3000
            });
          }
        });
      }
    }
  }

  //Decode the image from base64 to display it
  decodeImage(image: string): string {
    return `data:image/jpeg;base64,${image}`;
  }

  ngOnDestroy(): void {
    this.ngUnsubscribe.next();
    this.ngUnsubscribe.complete();
  }

  onFileSelected(event, task: Task): void {
    let file: File = event.target.files[0];
    const fileType = file.type;
    const maxSize = 1;  //max file size in MB, if over then the photo is compressed before uploading

    if (fileType.match(/image\/*/)) {
      if (this.imageUrls[task._id] && !window.confirm('An image has already been uploaded for this task. Are you sure you want to replace it?')) {
        return;
      }

      const compressAndUploadImage = () => {
        const reader = new FileReader();
        reader.readAsDataURL(file);
        reader.onload = (event: ProgressEvent<FileReader>) => {
          this.imageUrls[task._id] = event.target.result.toString();
        };

        if (file) {
          if (task.photos.length > 0) {
            this.replacePhoto(this.teamId, task, file);
          }
          else {
            this.submitPhoto(this.startedHunt._id, this.teamId, task, file);
          }
        }
      };

      if (file.size > maxSize * 1024 * 1024) {
        console.log('compressing image');
        this.ng2ImgMax.compressImage(file, maxSize).subscribe(
          result => {
            // Get the original file's extension
            const extension = file.name.split('.').pop();

            // Create a new File object for the compressed file, preserving the original file's extension
            file = new File([result], `compressed.${extension}`, { type: result.type });
            compressAndUploadImage();  // compress and upload image
          },
          error => {
            console.error('Error compressing image', error);
            this.snackBar.open('Error compressing image. Please try again', 'Close', {
              duration: 3000
            });
          }
        );
      } else {
        compressAndUploadImage();  // upload image without compression
      }
    }
  }

  submitPhoto(startedHuntId: string, teamId: string, task: Task, file: File): void {
    this.submissionService.submitPhoto(startedHuntId, teamId, task._id, file).subscribe({
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

  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  deletePhoto(task: Task, startedHuntId: string): void {
    this.submissionService.getSubmissionsByTeamAndTask(this.teamId, task._id).subscribe({
      next: (submission: Submission) => {
        console.log('submissionId', submission._id);
        this.submissionService.deleteSubmission(submission._id).subscribe({
          next: () => {
            task.status = false;
            task.photos = [];
            this.snackBar.open('Photo deleted successfully', 'Close', {
              duration: 3000
            });
          },
          error: (error: Error) => {
            console.error('Error deleting photo', error);
            this.snackBar.open('Error deleting photo. Please try again', 'Close', {
              duration: 3000
            });
          },
        });
      },
      error: (error: Error) => {
        console.error('Error getting submission', error);
        this.snackBar.open('Error getting submission. Please try again', 'Close', {
          duration: 3000
        });
      },
    });
  }


  replacePhoto(teamId: string, task: Task, file: File): void {
    this.submissionService.replacePhoto(teamId, task._id, file).subscribe({
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

  /*   allTasksCompleted(): boolean {
      return this.tasks.every(task => task.status);
    } */
}
