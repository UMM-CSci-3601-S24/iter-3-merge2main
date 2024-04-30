import { of, Subject, throwError } from 'rxjs';
import { ActivatedRoute, ParamMap } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { TestBed, ComponentFixture, tick, fakeAsync } from '@angular/core/testing';
import { HunterViewComponent } from './hunter-view.component';
import { HostService } from 'src/app/hosts/host.service';
import { StartedHunt } from 'src/app/startHunt/startedHunt'
import { Task } from 'src/app/hunts/task';
import { Ng2ImgMaxService } from 'ng2-img-max';
import { StartedHuntService } from 'src/app/startHunt/startedHunt.service';
import { SubmissionService } from 'src/app/submissions/submission.service';

describe('HunterViewComponent', () => {
  let component: HunterViewComponent;
  let fixture: ComponentFixture<HunterViewComponent>;
  let mockHostService: jasmine.SpyObj<HostService>;
  let mockStartedHuntService: jasmine.SpyObj<StartedHuntService>;
  let mockSubmissionService: jasmine.SpyObj<SubmissionService>;
  let mockRoute: { paramMap: Subject<ParamMap> };
  let mockSnackBar: jasmine.SpyObj<MatSnackBar>;
  let mockNg2ImgMax: jasmine.SpyObj<Ng2ImgMaxService>;

  beforeEach(async () => {
    //mockHostService = jasmine.createSpyObj('HostService', ['getStartedHunt', 'submitPhoto', 'replacePhoto']);
    mockSubmissionService = jasmine.createSpyObj('SubmissionService', ['submitPhoto', 'replacePhoto', 'getPhotoFromSubmission']);
    mockStartedHuntService = jasmine.createSpyObj('StartedHuntService', ['getStartedHunt']);
    mockRoute = {
      paramMap: new Subject<ParamMap>()
    };
    mockSnackBar = jasmine.createSpyObj('MatSnackBar', ['open']);
    mockNg2ImgMax = jasmine.createSpyObj('Ng2ImgMaxService', ['compressImage']);

    await TestBed.configureTestingModule({
      imports: [HunterViewComponent],
      providers: [
        { provide: HostService, useValue: mockHostService },
        { provide: ActivatedRoute, useValue: mockRoute },
        { provide: MatSnackBar, useValue: mockSnackBar },
        { provide: Ng2ImgMaxService, useValue: mockNg2ImgMax },
        { provide: StartedHuntService, useValue: mockStartedHuntService },
        { provide: SubmissionService, useValue: mockSubmissionService }
      ]
    })
      .compileComponents();

    fixture = TestBed.createComponent(HunterViewComponent);
    component = fixture.componentInstance;

    const initialStartedHunt: StartedHunt = {
      _id: '',
      completeHunt: {
        hunt: {
          _id: '',
          hostId: '',
          name: '',
          description: '',
          est: 0,
          numberOfTasks: 0
        },
        tasks: []
      },
      accessCode: ''
    };
    component.startedHunt = initialStartedHunt;

    fixture.detectChanges();
  });

  // it('should navigate to the right hunt page by access code', () => {
  //   const startedHunt: StartedHunt = {
  //     _id: '1',
  //     completeHunt: {
  //       hunt: {
  //         _id: '1',
  //         hostId: '1',
  //         name: 'Hunt 1',
  //         description: 'Hunt 1 Description',
  //         est: 10,
  //         numberOfTasks: 1
  //       },
  //       tasks: [
  //         {
  //           _id: '1',
  //           huntId: '1',
  //           name: 'Task 1',
  //           status: true,
  //           photos: []
  //         }
  //       ]
  //     },
  //     accessCode: '123456',
  //   };
  //   mockStartedHuntService.getStartedHunt.and.returnValue(of(startedHunt));
  //   // Emit a paramMap event to trigger the hunt retrieval
  //   mockRoute.paramMap.next({ get: () => '123456', has: () => true, getAll: () => [], keys: [] });
  //   component.ngOnInit();
  //   expect(component.startedHunt).toEqual(startedHunt);

  // });

  it('should handle error when getting hunt by access code', () => {
    const error = { message: 'Error', error: { title: 'Error Title' } };
    mockStartedHuntService.getStartedHunt.and.returnValue(throwError(error));
    // Emit a paramMap event to trigger the hunt retrieval
    mockRoute.paramMap.next({ get: () => '1', has: () => true, getAll: () => [], keys: [] });
    component.ngOnInit();
    expect(component.error).toEqual({
      help: 'There is an error trying to load the tasks or the submissions - Please try to run the hunt again',
      httpResponse: 'this.submissionService.getSubmissionsByTeam is not a function',
      message: 'An unexpected error occurred',
    });
  });

  it('should handle file selected event', () => {
    const task: Task = { _id: '1', huntId: '1', name: 'Task 1', status: true, photos: [] };
    const event = {
      target: {
        files: [
          new File([''], 'photo.jpg', { type: 'image/jpeg' })
        ]
      }
    };
    const reader = jasmine.createSpyObj('FileReader', ['readAsDataURL']);
    reader.onload = null;
    spyOn(window, 'FileReader').and.returnValue(reader);

    mockSubmissionService.submitPhoto.and.returnValue(of(undefined));

    component.onFileSelected(event, task);

    expect(reader.readAsDataURL).toHaveBeenCalledWith(event.target.files[0]);

    reader.onload({ target: { result: 'data:image/jpeg;base64,' } } as ProgressEvent<FileReader>);

    expect(component.imageUrls[task._id]).toBe('data:image/jpeg;base64,');
  });

  it('should not replace image if user choose cancel', () => {
    const task: Task = { _id: '1', huntId: '1', name: 'Task 1', status: true, photos: [] };
    const event = {
      target: {
        files: [
          {
            type: 'image/png',
            result: 'data:image/png;base64,'
          }
        ]
      }
    };
    const reader = jasmine.createSpyObj('FileReader', ['readAsDataURL', 'onload']);
    spyOn(window, 'FileReader').and.returnValue(reader);

    component.imageUrls[task._id] = 'data:image/png;base64,';
    spyOn(window, 'confirm').and.returnValue(false);

    component.onFileSelected(event, task);

    expect(reader.readAsDataURL).not.toHaveBeenCalled();
  });

  it('should replace image if user choose ok', () => {
    const task: Task = { _id: '1', huntId: '1', name: 'Task 1', status: true, photos: [] };
    const event = {
      target: {
        files: [
          new File([''], 'photo.jpg', { type: 'image/jpeg' })
        ]
      }
    };
    const reader = jasmine.createSpyObj('FileReader', ['readAsDataURL']);
    reader.onload = null;
    spyOn(window, 'FileReader').and.returnValue(reader);

    component.imageUrls[task._id] = 'data:image/png;base64,';
    spyOn(window, 'confirm').and.returnValue(true);

    mockSubmissionService.submitPhoto.and.returnValue(of(undefined));

    component.onFileSelected(event, task);

    expect(reader.readAsDataURL).toHaveBeenCalledWith(event.target.files[0]);

    reader.onload({ target: { result: 'data:image/jpeg;base64,' } } as ProgressEvent<FileReader>);

    expect(component.imageUrls[task._id]).toBe('data:image/jpeg;base64,');
  });

  it('should handle error when submitting photo', fakeAsync(() => {
    const task: Task = { _id: '1', huntId: '1', name: 'Task 1', status: true, photos: [] };
    const event = {
      target: {
        files: [
          new File([''], 'photo.jpg', { type: 'image/jpeg' })
        ]
      }
    };
    const reader = jasmine.createSpyObj('FileReader', ['readAsDataURL']);
    reader.onload = null;
    spyOn(window, 'FileReader').and.returnValue(reader);

    mockSubmissionService.submitPhoto.and.returnValue(throwError(() => new Error('Error')));

    component.onFileSelected(event, task);

    expect(reader.readAsDataURL).toHaveBeenCalledWith(event.target.files[0]);

    reader.onload({ target: { result: 'data:image/jpeg;base64,' } } as ProgressEvent<FileReader>);

    tick();

    expect(mockSnackBar.open).toHaveBeenCalledWith('Error uploading photo. Please try again', 'Close', {
      duration: 3000
    });
  }));

  describe('submitPhoto and replacePhoto', () => {
    let task: Task;
    let file: File;
    let startedHuntId: string;
    let teamId: string;

    beforeEach(() => {
      task = { _id: '1', huntId: '1', name: 'Task 1', status: true, photos: ['photoId'] };
      file = new File([''], 'photo.jpg', { type: 'image/jpeg' });
      startedHuntId = '';
    });

    it('should call replacePhoto when task has photos', () => {
      task = { _id: '1', huntId: '1', name: 'Task 1', status: true, photos: ['photoId'] };
      spyOn(component, 'replacePhoto');
      component.onFileSelected({ target: { files: [file] } }, task);
      expect(component.replacePhoto).toHaveBeenCalledWith(teamId, task, file);
    });

    it('should call submitPhoto when task does not have photos', () => {
      task = { _id: '1', huntId: '1', name: 'Task 1', status: true, photos: [] };
      spyOn(component, 'submitPhoto');
      component.onFileSelected({ target: { files: [file] } }, task);
      expect(component.submitPhoto).toHaveBeenCalledWith(startedHuntId, teamId, task, file);
    });

    it('should display success message and update task when photo is uploaded successfully', () => {
      const newPhotoId = 'newPhotoId';
      mockSubmissionService.submitPhoto.and.returnValue(of(newPhotoId));
      component.submitPhoto(startedHuntId, teamId, task, file);
      expect(mockSnackBar.open).toHaveBeenCalledWith('Photo uploaded successfully', 'Close', { duration: 3000 });
      expect(task.status).toBeTrue();
      expect(task.photos).toContain(newPhotoId);
    });

    it('should display error message when photo upload fails', () => {
      mockSubmissionService.submitPhoto.and.returnValue(throwError('Error message'));
      component.submitPhoto(startedHuntId, teamId, task, file);
      expect(mockSnackBar.open).toHaveBeenCalledWith('Error uploading photo. Please try again', 'Close', { duration: 3000 });
    });

    it('should display success message when photo is replaced successfully', () => {
      mockSubmissionService.replacePhoto.and.returnValue(of('newPhotoId'));
      component.replacePhoto(teamId, task, file);
      expect(mockSnackBar.open).toHaveBeenCalledWith('Photo replaced successfully', 'Close', { duration: 3000 });
      expect(task.photos[0]).toEqual('newPhotoId');
    });

    it('should display error message when photo replacement fails', () => {
      mockSubmissionService.replacePhoto.and.returnValue(throwError('Error message'));
      component.replacePhoto(teamId, task, file);
      expect(mockSnackBar.open).toHaveBeenCalledWith('Error replacing photo. Please try again', 'Close', { duration: 3000 });
    });
  });


  describe('Image compression', () => {
    let file: File;
    let maxSize: number;
    let task: Task;

    beforeEach(() => {
      file = new File([''], 'photo.jpg', { type: 'image/jpeg' });
      maxSize = 1;
      task = { _id: '1', huntId: '1', name: 'Task 1', status: true, photos: [] };
    });

    it('should compress image when file size is larger than max size', () => {
      const blob = new Blob([new ArrayBuffer(maxSize * 1024 * 1024 + 1)], { type: 'image/jpeg' });
      file = new File([blob], 'photo.jpg', { type: 'image/jpeg' });
      const compressedFile = new File([''], 'compressed.jpg', { type: 'image/jpeg' });
      mockNg2ImgMax.compressImage.and.returnValue(of(compressedFile));
      mockSubmissionService.submitPhoto.and.returnValue(of('photoId'));
      component.onFileSelected({ target: { files: [file] } }, task);
      expect(mockNg2ImgMax.compressImage).toHaveBeenCalledWith(file, maxSize);
    });

    it('should display error message when image compression fails', () => {
      const blob = new Blob([new ArrayBuffer(maxSize * 1024 * 1024 + 1)], { type: 'image/jpeg' });
      file = new File([blob], 'photo.jpg', { type: 'image/jpeg' });
      mockNg2ImgMax.compressImage.and.returnValue(throwError('Error message'));
      component.onFileSelected({ target: { files: [file] } }, task);
      expect(mockSnackBar.open).toHaveBeenCalledWith('Error compressing image. Please try again', 'Close', { duration: 3000 });
    });
  });

  describe('Photo Deletion', () => {
    let component: HunterViewComponent;
    let mockHostService: jasmine.SpyObj<HostService>;
    let mockSnackBar: jasmine.SpyObj<MatSnackBar>;
    let task: Task;
    let startedHuntId: string;
    const mockRoute = jasmine.createSpyObj('ActivatedRoute', ['snapshot']);
    const mockRouter = jasmine.createSpyObj('Router', ['navigate']);
    const mockDialog = jasmine.createSpyObj('MatDialog', ['open']);
    const mockNg2ImgMax = jasmine.createSpyObj('Ng2ImgMaxService', ['compressImage']);

    beforeEach(() => {
      mockHostService = jasmine.createSpyObj('HostService', ['deletePhoto']);
      mockSnackBar = jasmine.createSpyObj('MatSnackBar', ['open']);
      component = new HunterViewComponent(mockHostService, mockRoute, mockSnackBar, mockRouter, mockDialog, mockNg2ImgMax);
      task = { _id: '1', huntId: '1', name: 'Task 1', status: true, photos: ['photoId']};
      startedHuntId = '';
    });

    it('should handle successful photo deletion', () => {
      // Arrange: Set up the deletePhoto method to return an observable that completes.
      mockHostService.deletePhoto.and.returnValue(of(null));

      // Act: Call the method that triggers the deletion.
      component.deletePhoto(task, startedHuntId);

      // Assert: Check that the success message was shown.
      expect(mockSnackBar.open).toHaveBeenCalledWith('Photo deleted successfully', 'Close', { duration: 3000 });

      // Assert: Check that the task status was set to false and photos array was emptied.
      expect(task.status).toBeFalse();
      expect(task.photos).toEqual([]);
    });

    it('should handle error when deleting photo', () => {
      // Arrange: Set up the deletePhoto method to return an observable that throws an error.
      const error = new Error('Test error');
      mockHostService.deletePhoto.and.returnValue(throwError(error));

      // Act: Call the method that triggers the deletion.
      component.deletePhoto(task, startedHuntId);

      // Assert: Check that the error message was shown.
      expect(mockSnackBar.open).toHaveBeenCalledWith('Error deleting photo. Please try again', 'Close', { duration: 3000 });
    });
  });



});
