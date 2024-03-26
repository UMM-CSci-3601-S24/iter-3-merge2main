import { of, Subject, throwError } from 'rxjs';
import { ActivatedRoute, ParamMap } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { TestBed, ComponentFixture } from '@angular/core/testing';
import { HunterViewComponent } from './hunter-view.component';
import { HostService } from 'src/app/hosts/host.service';
import { CompleteHunt } from 'src/app/hunts/completeHunt';
import { Task } from 'src/app/hunts/task';

describe('HunterViewComponent', () => {
  let component: HunterViewComponent;
  let fixture: ComponentFixture<HunterViewComponent>;
  let mockHostService: jasmine.SpyObj<HostService>;
  let mockRoute: { paramMap: Subject<ParamMap> };
  let mockSnackBar: jasmine.SpyObj<MatSnackBar>;

  beforeEach(async () => {
    mockHostService = jasmine.createSpyObj(['getHuntById']);
    mockRoute = {
      paramMap: new Subject<ParamMap>()
    };
    mockSnackBar = jasmine.createSpyObj(['open']);

    await TestBed.configureTestingModule({
      declarations: [HunterViewComponent],
      providers: [
        { provide: HostService, useValue: mockHostService },
        { provide: ActivatedRoute, useValue: mockRoute },
        { provide: MatSnackBar, useValue: mockSnackBar }
      ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(HunterViewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize with a hunt from the host service', () => {
    const completeHunt: CompleteHunt = {
      hunt: {
        _id: 'some_id',
        hostId: 'some_hostId',
        name: 'some_name',
        description: 'some_description',
        est: 120,
        numberOfTasks: 5},
      tasks: []
    };
    mockHostService.getHuntById.and.returnValue(of(completeHunt));
    mockRoute.paramMap.next({ get: () => '1', has: () => true, getAll: () => [], keys: [] });

    component.ngOnInit();

    expect(component.completeHunt).toBe(completeHunt);
    expect(component.tasks).toBe(completeHunt.tasks);
  });

  it('should handle error when getting hunt by id', () => {
    const error = { message: 'Error', error: { title: 'Error Title' } };
    mockHostService.getHuntById.and.returnValue(throwError(error));
    mockRoute.paramMap.next({ get: () => '1', has: () => true, getAll: () => [], keys: [] });

    component.ngOnInit();

    expect(component.error).toEqual({
      help: 'There is an error trying to load the tasks - Please try to run the hunt again',
      httpResponse: error.message,
      message: error.error.title,
    });
  });

  it('should replace any with a specific type', () => {
    // Assuming a function or service call previously used 'any'
    const specificTypeExample: SpecificType = service.callFunction(); // SpecificType is a placeholder
    expect(specificTypeExample).toBeDefined();
  });

  it('should handle file selected event', () => {
    const task: Task = { _id: '1', huntId: '1', name: 'Task 1', status: true };
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
    const reader = jasmine.createSpyObj(['readAsDataURL', 'onload']);
    spyOn(window, 'FileReader').and.returnValue(reader);

    component.onFileSelected(event, task);

    expect(reader.readAsDataURL).toHaveBeenCalledWith(event.target.files[0]);
    reader.onload(event);
    expect(component.imageUrls[task._id]).toBe(event.target.files[0].result);
  });
});
