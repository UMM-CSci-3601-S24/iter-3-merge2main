import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { MockHostService } from 'src/testing/host.service.mock';
import { EndedHuntDetailsComponent } from './ended-hunt.details.component';
import { HostService } from 'src/app/hosts/host.service';
import { HttpClientModule } from '@angular/common/http';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { MatCardModule } from '@angular/material/card';
import { RouterTestingModule } from '@angular/router/testing';
import { EndedHuntCardComponent } from '../ended-hunt-card.component';
import { ActivatedRoute } from '@angular/router';
import { ActivatedRouteStub } from 'src/testing/activated-route-stub';
import { EndedHunt } from '../endedHunt';
import { MatDialog } from '@angular/material/dialog';
import { PhotoDialogComponent } from './photo-dialog/photo-dialog.component';
import { of } from 'rxjs';

describe('EndedHuntDetailsComponent', () => {
  let component: EndedHuntDetailsComponent;
  let fixture: ComponentFixture<EndedHuntDetailsComponent>;
  const mockHostService = new MockHostService();
  const chrisId = 'chris_id';
  const activatedRoute: ActivatedRouteStub = new ActivatedRouteStub({
    id: chrisId,
  });
  let dialog: MatDialog;
  let dialogSpy: jasmine.Spy;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [
        HttpClientModule,
        RouterTestingModule,
        MatCardModule,
        EndedHuntDetailsComponent,
        EndedHuntCardComponent,
        HttpClientModule,
        HttpClientTestingModule,
      ],
      providers: [
        { provide: HostService, useValue: mockHostService },
        { provide: ActivatedRoute, useValue: activatedRoute },
        { provide: MatDialog, useValue: { open: jasmine.createSpy('open') } },
      ],
    }).compileComponents();

  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(EndedHuntDetailsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
    dialog = TestBed.inject(MatDialog);
    dialogSpy = dialog.open as jasmine.Spy;
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should get task name', () => {
    const mockEndedHunt: EndedHunt = {
      startedHunt: {
        _id: 'some_id',
        accessCode: 'some_access_code',
        completeHunt: {
          hunt: {
            _id: 'hunt_id',
            hostId: 'host_id',
            name: 'hunt_name',
            description: 'hunt_description',
            est: 20,
            numberOfTasks: 4
          },
          tasks: [
            {
              _id: 'task1', name: 'Task 1',
              huntId: '',
              status: false,
              photos: []
            },
            {
              _id: 'task2', name: 'Task 2',
              huntId: '',
              status: false,
              photos: []
            },
          ],
        },
      },
      finishedTasks: [],
    };
    component.endedHunt = mockEndedHunt;
    expect(component.getTaskName('task1')).toEqual('Task 1');
    expect(component.getTaskName('task2')).toEqual('Task 2');
  });

  it('should open dialog', () => {
    const mockPhoto = 'photo1';
    const mockPhotos = ['photo1', 'photo2', 'photo3'];
    component.openDialog(mockPhoto, mockPhotos);
    expect(dialogSpy).toHaveBeenCalledWith(PhotoDialogComponent, {
      data: {
        currentPhoto: mockPhoto,
        photos: mockPhotos,
      },
    });
  });
  it('should set endedHunt on ngOnInit', () => {
    const mockEndedHunt: EndedHunt = {
      startedHunt: {
        _id: 'some_id',
        accessCode: 'some_access_code',
        completeHunt: {
          hunt: {
            _id: 'hunt_id',
            hostId: 'host_id',
            name: 'hunt_name',
            description: 'hunt_description',
            est: 20,
            numberOfTasks: 4
          },
          tasks: [
            {
              _id: 'task1', name: 'Task 1',
              huntId: '',
              status: false,
              photos: []
            },
            {
              _id: 'task2', name: 'Task 2',
              huntId: '',
              status: false,
              photos: []
            },
          ],
        },
      },
      finishedTasks: [],
    };

    spyOn(mockHostService, 'getEndedHuntById').and.returnValue(of(mockEndedHunt));

    component.ngOnInit();

    expect(component.endedHunt).toEqual(mockEndedHunt);
  });
});
