import { of, Subject, throwError } from 'rxjs';
import { ActivatedRoute, ParamMap } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { TestBed, ComponentFixture, tick, fakeAsync } from '@angular/core/testing';
import { HunterViewComponent } from './hunter-view.component';
import { HostService } from 'src/app/hosts/host.service';
import { StartedHunt } from 'src/app/startHunt/startedHunt'
import { Task } from 'src/app/hunts/task';
import { Ng2ImgMaxService } from 'ng2-img-max';

describe('HunterViewComponent', () => {
  let component: HunterViewComponent;
  let fixture: ComponentFixture<HunterViewComponent>;
  let mockHostService: jasmine.SpyObj<HostService>;
  let mockRoute: { paramMap: Subject<ParamMap> };
  let mockSnackBar: jasmine.SpyObj<MatSnackBar>;
  let mockNg2ImgMax: jasmine.SpyObj<Ng2ImgMaxService>;

  beforeEach(async () => {
    mockHostService = jasmine.createSpyObj('HostService', ['getStartedHunt', 'submitPhoto', 'replacePhoto']);
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
        { provide: Ng2ImgMaxService, useValue: mockNg2ImgMax }
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

  it('should navigate to the right hunt page by access code', () => {
    const startedHunt: StartedHunt = {
      _id: '',
      completeHunt: {
        hunt: {
          _id: '1',
          hostId: '1',
          name: 'Hunt 1',
          description: 'Hunt 1 Description',
          est: 10,
          numberOfTasks: 1
        },
        tasks: [
          {
            _id: '1',
            huntId: '1',
            name: 'Task 1',
            status: true,
            photos: []
          }
        ]
      },
      accessCode: '123456'
    };
    mockHostService.getStartedHunt.and.returnValue(of(startedHunt));
    // Emit a paramMap event to trigger the hunt retrieval
    mockRoute.paramMap.next({ get: () => '123456', has: () => true, getAll: () => [], keys: [] });
    component.ngOnInit();
    expect(component.startedHunt).toEqual(startedHunt);

  });

  it('should handle error when getting hunt by access code', () => {
    const error = { message: 'Error', error: { title: 'Error Title' } };
    mockHostService.getStartedHunt.and.returnValue(throwError(error));
    // Emit a paramMap event to trigger the hunt retrieval
    mockRoute.paramMap.next({ get: () => '1', has: () => true, getAll: () => [], keys: [] });
    component.ngOnInit();
    expect(component.error).toEqual({
      help: 'There is an error trying to load the tasks - Please try to run the hunt again',
      httpResponse: error.message,
      message: error.error.title,
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

    mockHostService.submitPhoto.and.returnValue(of(undefined));

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

    mockHostService.submitPhoto.and.returnValue(of(undefined));

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

    mockHostService.submitPhoto.and.returnValue(throwError(() => new Error('Error')));

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

    beforeEach(() => {
      task = { _id: '1', huntId: '1', name: 'Task 1', status: true, photos: ['photoId'] };
      file = new File([''], 'photo.jpg', { type: 'image/jpeg' });
      startedHuntId = '';
    });

    it('should call replacePhoto when task has photos', () => {
      task = { _id: '1', huntId: '1', name: 'Task 1', status: true, photos: ['photoId'] };
      spyOn(component, 'replacePhoto');
      component.onFileSelected({ target: { files: [file] } }, task);
      expect(component.replacePhoto).toHaveBeenCalledWith(file, task, startedHuntId);
    });

    it('should call submitPhoto when task does not have photos', () => {
      task = { _id: '1', huntId: '1', name: 'Task 1', status: true, photos: [] };
      spyOn(component, 'submitPhoto');
      component.onFileSelected({ target: { files: [file] } }, task);
      expect(component.submitPhoto).toHaveBeenCalledWith(file, task, startedHuntId);
    });

    it('should display success message and update task when photo is uploaded successfully', () => {
      const newPhotoId = 'newPhotoId';
      mockHostService.submitPhoto.and.returnValue(of(newPhotoId));
      component.submitPhoto(file, task, startedHuntId);
      expect(mockSnackBar.open).toHaveBeenCalledWith('Photo uploaded successfully', 'Close', { duration: 3000 });
      expect(task.status).toBeTrue();
      expect(task.photos).toContain(newPhotoId);
    });

    it('should display error message when photo upload fails', () => {
      mockHostService.submitPhoto.and.returnValue(throwError('Error message'));
      component.submitPhoto(file, task, startedHuntId);
      expect(mockSnackBar.open).toHaveBeenCalledWith('Error uploading photo. Please try again', 'Close', { duration: 3000 });
    });

    it('should display success message when photo is replaced successfully', () => {
      mockHostService.replacePhoto.and.returnValue(of('newPhotoId'));
      component.replacePhoto(file, task, startedHuntId);
      expect(mockSnackBar.open).toHaveBeenCalledWith('Photo replaced successfully', 'Close', { duration: 3000 });
      expect(task.photos[0]).toEqual('newPhotoId');
    });

    it('should display error message when photo replacement fails', () => {
      mockHostService.replacePhoto.and.returnValue(throwError('Error message'));
      component.replacePhoto(file, task, startedHuntId);
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
      mockHostService.submitPhoto.and.returnValue(of('photoId'));  // Mock hostService.submitPhoto here
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


});

