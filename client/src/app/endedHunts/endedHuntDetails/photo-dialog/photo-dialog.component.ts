import { Component, HostListener, Inject } from '@angular/core';
import { MAT_DIALOG_DATA } from '@angular/material/dialog';

@Component({
  selector: 'app-photo-dialog',
  template: `
    <img [src]="data.currentPhoto" alt="Enlarged Photo" class="enlarged-photo">
    <button (click)="previousPhoto()">Previous</button>
    <button (click)="nextPhoto()">Next</button>
  `
})
export class PhotoDialogComponent {
  currentPhotoIndex: number;

  constructor(@Inject(MAT_DIALOG_DATA) public data: { currentPhoto: string; photos: string[] }) {
    this.currentPhotoIndex = data.photos.indexOf(data.currentPhoto);
  }

  @HostListener('window:keydown', ['$event'])
  handleKeyDown(event: KeyboardEvent): void {
    if (event.key === 'ArrowLeft') {
      this.previousPhoto();
    } else if (event.key === 'ArrowRight') {
      this.nextPhoto();
    }
  }

  previousPhoto(): void {
    if (this.currentPhotoIndex > 0) {
      this.currentPhotoIndex--;
      this.data.currentPhoto = this.data.photos[this.currentPhotoIndex];
    }
  }

  nextPhoto(): void {
    if (this.currentPhotoIndex < this.data.photos.length - 1) {
      this.currentPhotoIndex++;
      this.data.currentPhoto = this.data.photos[this.currentPhotoIndex];
    }
  }
}
