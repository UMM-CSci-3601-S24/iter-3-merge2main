import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatDialogModule, MAT_DIALOG_DATA } from '@angular/material/dialog';

import { PhotoDialogComponent } from './photo-dialog.component';

describe('PhotoDialogComponent', () => {
  let component: PhotoDialogComponent;
  let fixture: ComponentFixture<PhotoDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MatDialogModule],
      declarations: [PhotoDialogComponent],
      providers: [
        { provide: MAT_DIALOG_DATA, useValue: { currentPhoto: 'photo1', photos: ['photo1', 'photo2', 'photo3'] } }
      ]
    })
      .compileComponents();

    fixture = TestBed.createComponent(PhotoDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should go to the previous photo', () => {
    component.currentPhotoIndex = 1;
    component.previousPhoto();
    expect(component.currentPhotoIndex).toBe(0);
    expect(component.data.currentPhoto).toBe('photo1');
  });

  it('should not go to the previous photo if it is the first one', () => {
    component.currentPhotoIndex = 0;
    component.previousPhoto();
    expect(component.currentPhotoIndex).toBe(0);
    expect(component.data.currentPhoto).toBe('photo1');
  });

  it('should go to the next photo', () => {
    component.currentPhotoIndex = 1;
    component.nextPhoto();
    expect(component.currentPhotoIndex).toBe(2);
    expect(component.data.currentPhoto).toBe('photo3');
  });

  it('should not go to the next photo if it is the last one', () => {
    component.currentPhotoIndex = 2;
    component.data.currentPhoto = component.data.photos[component.currentPhotoIndex];
    component.nextPhoto();
    expect(component.currentPhotoIndex).toBe(2);
    expect(component.data.currentPhoto).toBe('photo3');
  });

  it('should go to the previous photo when ArrowLeft key is pressed', () => {
    component.currentPhotoIndex = 1;
    const event = new KeyboardEvent('keydown', { key: 'ArrowLeft' });
    component.handleKeyDown(event);
    expect(component.currentPhotoIndex).toBe(0);
    expect(component.data.currentPhoto).toBe('photo1');
  });

  it('should go to the next photo when ArrowRight key is pressed', () => {
    component.currentPhotoIndex = 1;
    const event = new KeyboardEvent('keydown', { key: 'ArrowRight' });
    component.handleKeyDown(event);
    expect(component.currentPhotoIndex).toBe(2);
    expect(component.data.currentPhoto).toBe('photo3');
  });

  it('should not change photo when other keys are pressed', () => {
    component.currentPhotoIndex = 1;
    component.data.currentPhoto = component.data.photos[component.currentPhotoIndex];
    const event = new KeyboardEvent('keydown', { key: 'ArrowUp' });
    component.handleKeyDown(event);
    expect(component.currentPhotoIndex).toBe(1);
    expect(component.data.currentPhoto).toBe('photo2');
  });
});
