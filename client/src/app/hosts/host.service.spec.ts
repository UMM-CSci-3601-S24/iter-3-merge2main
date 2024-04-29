import { HttpClient } from '@angular/common/http';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { TestBed, waitForAsync } from '@angular/core/testing';
import { of } from 'rxjs';
import { Hunt } from '../hunts/hunt';
import { Task } from '../hunts/task';
import { HostService } from './host.service';
import { StartedHunt } from '../startHunt/startedHunt';

describe('HostService', () => {
const testHunts: Hunt[] = [
  {
    _id: "5889",
    hostId: "588",
    name: "Default Hunt 1",
    description: "This is the default hunt 1",
    est: 20,
    numberOfTasks: 4
  },
  {
    _id: "5754",
    hostId: "575",
    name: "Default Hunt 2",
    description: "This is the default hunt 2",
    est: 17,
    numberOfTasks: 4
  },
  {
    _id: "de7c",
    hostId: "e7c",
    name: "Default Hunt 3",
    description: "This is the default hunt 3",
    est: 15,
    numberOfTasks: 4
  },
];

const testTasks: Task[] = [
  {
    _id: "5889",
    huntId: "588",
    name: "Default Task 1",
    status: false,
    photos: []
  },
  {
    _id: "5754",
    huntId: "575",
    name: "Default Task 2",
    status: false,
    photos: []
  },
  {
    _id: "de7c",
    huntId: "e7c",
    name: "Default Task 3",
    status: false,
    photos: []
  },
];

const testStartedHunts: StartedHunt[] = [
  {
    _id: "1234",
    completeHunt: {
      hunt: testHunts[0],
      tasks: testTasks
    },
    accessCode: "1234",
  },
  {
    _id: "5678",
    completeHunt: {
      hunt: testHunts[1],
      tasks: testTasks
    },
    accessCode: "5678",
  },
  {
    _id: "9012",
    completeHunt: {
      hunt: testHunts[2],
      tasks: testTasks
    },
    accessCode: "9012",
  },

];

let hostService: HostService;
let httpClient: HttpClient;
let httpTestingController: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule]
  });
  httpClient = TestBed.inject(HttpClient);
  httpTestingController = TestBed.inject(HttpTestingController);
  hostService = new HostService(httpClient);
  });

afterEach(() => {
  httpTestingController.verify();
});

describe('When getHunts() is called', () => {

  it('calls `api/hosts/575`', waitForAsync(() => {

    const mockedMethod = spyOn(httpClient, 'get').and.returnValue(of(testHunts));

    hostService.getHunts("575").subscribe(() => {

     expect(mockedMethod)
      .withContext('one call')
      .toHaveBeenCalledTimes(1);

     expect(mockedMethod)
      .withContext('talks to the correct endpoint')
      .toHaveBeenCalledWith(`${hostService.hostUrl}/575`);
    });
  }));
 });

 describe('When getHuntById() is given an ID', () => {
  it('calls api/hunts/id with the correct ID', waitForAsync(() => {
    const targetHunt: Hunt = testHunts[1];
    const targetId: string = targetHunt._id;

    const mockedMethod = spyOn(httpClient, 'get').and.returnValue(of(targetHunt));

      hostService.getHuntById(targetId).subscribe(() => {
        expect(mockedMethod)
          .withContext('one call')
          .toHaveBeenCalledTimes(1);

        expect(mockedMethod)
          .withContext('talks to the correct endpoint')
          .toHaveBeenCalledWith(`${hostService.huntUrl}/${targetId}`);
      });
    }));
  });

  describe('Adding a hunt using `addHunt()`', () => {
    it('talks to the right endpoint and is called once', waitForAsync(() => {
      const hunt_id = 'pat_id';
      const expected_http_response = { id: hunt_id } ;

      const mockedMethod = spyOn(httpClient, 'post')
        .and
        .returnValue(of(expected_http_response));

      hostService.addHunt(testHunts[1]).subscribe((new_hunt_id) => {
        expect(new_hunt_id).toBe(hunt_id);
        expect(mockedMethod)
          .withContext('one call')
          .toHaveBeenCalledTimes(1);
        expect(mockedMethod)
          .withContext('talks to the correct endpoint')
          .toHaveBeenCalledWith(hostService.huntUrl, testHunts[1]);
      });
    }));
  });

  describe('Adding a task using `addTask()`', () => {
    it('talks to the right endpoint and is called once', waitForAsync(() => {
      const task_id = 'hunt_id';
      const expected_http_response = { id: task_id } ;

      const mockedMethod = spyOn(httpClient, 'post')
        .and
        .returnValue(of(expected_http_response));

      hostService.addTask(testTasks[1]).subscribe((new_task_id) => {
        expect(new_task_id).toBe(task_id);
        expect(mockedMethod)
          .withContext('one call')
          .toHaveBeenCalledTimes(1);
        expect(mockedMethod)
          .withContext('talks to the correct endpoint')
          .toHaveBeenCalledWith(hostService.taskUrl, testTasks[1]);
      });
    }));
  });

  describe('Deleting a hunt using `deleteHunt()`', () => {
    it('talks to the right endpoint and is called once', waitForAsync(() => {
      const hunt_id = 'hunt_id';

      const mockedMethod = spyOn(httpClient, 'delete')
        .and
        .returnValue(of(undefined));

      hostService.deleteHunt(hunt_id).subscribe(() => {
        expect(mockedMethod)
          .withContext('one call')
          .toHaveBeenCalledTimes(1);
        expect(mockedMethod)
          .withContext('talks to the correct endpoint')
          .toHaveBeenCalledWith(`/api/hunts/${hunt_id}`);
      });
    }));
  });

  describe('Deleting a task using `deleteTask()`', () => {
    it('talks to the right endpoint and is called once', waitForAsync(() => {
      const task_id = 'task_id';

      const mockedMethod = spyOn(httpClient, 'delete')
        .and
        .returnValue(of(undefined));

      hostService.deleteTask(task_id).subscribe(() => {
        expect(mockedMethod)
          .withContext('one call')
          .toHaveBeenCalledTimes(1);
        expect(mockedMethod)
          .withContext('talks to the correct endpoint')
          .toHaveBeenCalledWith(`/api/tasks/${task_id}`);
      });
    }));
  });

  describe('Starting a hunt using `startHunt()`', () => {
    it('calls api/startHunt/id with the correct ID', waitForAsync(() => {
      const targetStartedHunt: StartedHunt = testStartedHunts[1];
      const targetId: string = targetStartedHunt.completeHunt.hunt._id;

      const mockedMethod = spyOn(httpClient, 'get').and.returnValue(of(targetStartedHunt.accessCode));

        hostService.startHunt(targetId).subscribe(() => {
          expect(mockedMethod)
            .withContext('one call')
            .toHaveBeenCalledTimes(1);

          expect(mockedMethod)
            .withContext('talks to the correct endpoint')
            .toHaveBeenCalledWith(`${hostService.startHuntUrl}/${targetId}`);
        });
      }));
    });

  describe('When getStartedHunt() is given an ID', () => {
    it('calls api/startedHunts/id with the correct ID', waitForAsync(() => {
      const targetStartedHunt: StartedHunt = testStartedHunts[1];
      const targetAccessCode: string = targetStartedHunt.accessCode;

      const mockedMethod = spyOn(httpClient, 'get').and.returnValue(of(targetStartedHunt));

      hostService.getStartedHunt(targetAccessCode).subscribe(() => {
        expect(mockedMethod)
          .withContext('one call')
          .toHaveBeenCalledTimes(1);

        expect(mockedMethod)
          .withContext('talks to the correct endpoint')
          .toHaveBeenCalledWith(`${hostService.startedHuntUrl}/${targetAccessCode}`);
      });
    }));
  });

  describe('Ending a hunt using `endStartedHunt()`', () => {
    it('calls api/startedHunts/id with the correct ID', waitForAsync(() => {
      const targetStartedHunt: StartedHunt = testStartedHunts[1];
      const targetId: string = targetStartedHunt._id;

      const mockedMethod = spyOn(httpClient, 'put').and.returnValue(of(undefined));

      hostService.endStartedHunt(targetId).subscribe(() => {
        expect(mockedMethod)
          .withContext('one call')
          .toHaveBeenCalledTimes(1);

        expect(mockedMethod)
          .withContext('talks to the correct endpoint')
          .toHaveBeenCalledWith(`${hostService.endHuntUrl}/${targetId}`, null);
      });
    }));
  });

  describe('When getEndedHunts() is called', () => {
    it('calls api/hosts/{id}/endedHunts', waitForAsync(() => {
      const hostId = 'testHostId';
      const mockedMethod = spyOn(httpClient, 'get').and.returnValue(of(testStartedHunts));

      hostService.getEndedHunts(hostId).subscribe(() => {
        expect(mockedMethod)
          .withContext('one call')
          .toHaveBeenCalledTimes(1);

        expect(mockedMethod)
          .withContext('talks to the correct endpoint')
          .toHaveBeenCalledWith(`${hostService.hostUrl}/${hostId}/endedHunts`);
      });
    }));
  });

  describe('Deleting an ended hunt using `deleteEndedHunt()`', () => {
    it('talks to the right endpoint and is called once', waitForAsync(() => {
      const hunt_id = 'hunt_id';

      const mockedMethod = spyOn(httpClient, 'delete')
        .and
        .returnValue(of(undefined));

      hostService.deleteEndedHunt(hunt_id).subscribe(() => {
        expect(mockedMethod)
          .withContext('one call')
          .toHaveBeenCalledTimes(1);
        expect(mockedMethod)
          .withContext('talks to the correct endpoint')
          .toHaveBeenCalledWith(`${hostService.endedHuntsUrl}/${hunt_id}`);
      });
    }));
  });

  describe('Deleting an ended hunt using `deleteEndedHunt()`', () => {
    it('talks to the right endpoint and is called once', waitForAsync(() => {
      const hunt_id = 'hunt_id';

      const mockedMethod = spyOn(httpClient, 'delete')
        .and
        .returnValue(of(undefined));

      hostService.deleteEndedHunt(hunt_id).subscribe(() => {
        expect(mockedMethod)
          .withContext('one call')
          .toHaveBeenCalledTimes(1);
        expect(mockedMethod)
          .withContext('talks to the correct endpoint')
          .toHaveBeenCalledWith(`${hostService.endedHuntsUrl}/${hunt_id}`);
      });
    }));
  });

  describe('Submitting a photo using `submitPhoto()`', () => {
    it('talks to the right endpoint and is called once', waitForAsync(() => {
      const startedHuntId = 'startedHunt_id';
      const task_id = 'task_id';
      const photo = new File([''], 'photo.jpg', { type: 'image/jpeg' });

      const mockedMethod = spyOn(httpClient, 'post')
        .and
        .returnValue(of({id: 'someId'}));

      hostService.submitPhoto(startedHuntId, task_id, photo).subscribe(() => {
        expect(mockedMethod)
          .withContext('one call')
          .toHaveBeenCalledTimes(1);

        const args = mockedMethod.calls.first().args;

        expect(args[0])
        .withContext('talks to the correct endpoint')
        .toEqual(`${hostService.endedHuntUrl}/${startedHuntId}/tasks/${task_id}/photo`);

        const formData: FormData = args[1];
        expect(formData.get('photo')).toEqual(photo);
      });
    }));
  });

  describe('Replacing a photo using `replacePhoto()`', () => {
    it('talks to the right endpoint and is called once', waitForAsync(() => {
      const startedHuntId = 'startedHunt_id';
      const task_id = 'task_id';
      const photo = new File([''], 'photo.jpg', { type: 'image/jpeg' });
      const photoPath = 'photo.jpg';

      const mockedMethod = spyOn(httpClient, 'put')
        .and
        .returnValue(of({id: 'someId'}));

      hostService.replacePhoto(startedHuntId, task_id, photoPath ,photo).subscribe(() => {
        expect(mockedMethod)
          .withContext('one call')
          .toHaveBeenCalledTimes(1);

        const args = mockedMethod.calls.first().args;

        expect(args[0])
        .withContext('talks to the correct endpoint')
        .toEqual(`${hostService.endedHuntUrl}/${startedHuntId}/tasks/${task_id}/photo/${photoPath}`);

        const formData: FormData = args[1];
        expect(formData.get('photo')).toEqual(photo);
      });
    }));
  });

  describe('Deleting a photo using `deletePhoto()`', () => {
    it('talks to the right endpoint and is called once', waitForAsync(() => {
      const startedHuntId = 'startedHunt_id';
      const taskId = 'task_id';
      const photoPath = 'photo.jpg';

      const mockedMethod = spyOn(httpClient, 'delete')
        .and
        .returnValue(of(null));

      hostService.deletePhoto(startedHuntId, taskId, photoPath).subscribe(() => {
        expect(mockedMethod)
          .withContext('one call')
          .toHaveBeenCalledTimes(1);

        const args = mockedMethod.calls.first().args;

        expect(args[0])
        .withContext('talks to the correct endpoint')
        .toEqual(`${hostService.endedHuntUrl}/${startedHuntId}/tasks/${taskId}/photo/${photoPath}`);
      });
    }));
  });
});
