import { HttpClient } from "@angular/common/http";
import { HttpClientTestingModule, HttpTestingController } from "@angular/common/http/testing";
import { StartedHunt } from "./startedHunt";
import { StartedHuntService } from "./startedHunt.service";
import { Hunt } from "../hunts/hunt";
import { TestBed, waitForAsync } from "@angular/core/testing";
import { of } from "rxjs";
import { Task } from "../hunts/task";
import { MockStartedHuntService } from "src/testing/startedHunt.service.mock";

describe('StartedHuntService', () => {
  const testHunts: Hunt[] = [
    {
      _id: 'hunt1_id',
      hostId: 'ann_hid',
      name: 'Anns Hunt',
      description: 'exciting hunt',
      est: 30,
      numberOfTasks: 3,
    },
    {
      _id: 'hunt2_id',
      hostId: 'fran_hid',
      name: 'Frans Hunt',
      description: 'super exciting hunt',
      est: 45,
      numberOfTasks: 2,
    },
  ];

  const testTasks: Task[] = [
    {
      _id: "5889",
      huntId: "hunt1_id",
      name: "Default Task 1",
      status: false,
      photos: []
    },
    {
      _id: "5754",
      huntId: "hunt1_id",
      name: "Default Task 2",
      status: false,
      photos: []
    },
  ];

  const testStartedHunts: StartedHunt[] = [
    {
      _id: 'startedHunt1_id',
      accessCode: '123456',
      completeHunt: {
        hunt: testHunts[0],
        tasks: testTasks
      },
      status: true,
      submissionIds: ['1234', '5432'],
    },
    {
      _id: 'startedHunt2_id',
      accessCode: '654321',
      completeHunt: {
        hunt: testHunts[1],
        tasks: [
          {
            _id: '4545',
            huntId: 'hunt2_id',
            name: 'Take a picture of a restaurant',
            status: false,
            photos: [],
          },
          {
            _id: '5656',
            huntId: 'hunt2_id',
            name: 'Take a picture of a cat',
            status: false,
            photos: [],
          },
        ],
      },
      status: false,
      submissionIds: ['9876'],
    },
  ];

  let startedHuntService: StartedHuntService;
  let httpClient: HttpClient;
  let httpTestingController: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule]
    });

    httpClient = TestBed.inject(HttpClient);
    httpTestingController = TestBed.inject(HttpTestingController);
    startedHuntService = new StartedHuntService(httpClient);
  });

  afterEach(() => {
    httpTestingController.verify();
  });

  it('should be created', () => {
    expect(startedHuntService).toBeTruthy();
  });

  it('should get started hunt by access code', () => {
    const accessCode = '123456';
    const expectedStartedHunt = testStartedHunts[0];
    startedHuntService.getStartedHunt(accessCode).subscribe(
      startedHunt => expect(startedHunt).toEqual(expectedStartedHunt)
    );

    const req = httpTestingController.expectOne(`${startedHuntService.startedHuntsUrl}/${accessCode}`);
    expect(req.request.method).toEqual('GET');
    req.flush(expectedStartedHunt);
  });

  it('should get ended hunt by id', () => {
    const endedHuntId = 'startedHunt2_id';
    const expectedEndedHunt = testStartedHunts[1];
    startedHuntService.getEndedHuntById(endedHuntId).subscribe(
      endedHunt => expect(endedHunt).toEqual(expectedEndedHunt)
    );

    const req = httpTestingController.expectOne(`${startedHuntService.endedHuntsUrl}/${endedHuntId}`);
    expect(req.request.method).toEqual('GET');
    req.flush(expectedEndedHunt);
  });

  it('should get started hunt by id', () => {
    const startedHuntId = 'startedHunt1_id';
    const expectedStartedHunt = testStartedHunts[0];
    startedHuntService.getStartedHuntById(startedHuntId).subscribe(
      startedHunt => expect(startedHunt).toEqual(expectedStartedHunt)
    );

    const req = httpTestingController.expectOne(`${startedHuntService.startedHuntUrl}/${startedHuntId}`);
    expect(req.request.method).toEqual('GET');
    req.flush(expectedStartedHunt);
  });

  describe('Starting a hunt using `startHunt()`', () => {
    it('calls api/startHunt/id with the correct ID', waitForAsync(() => {
      const targetStartedHunt: StartedHunt = testStartedHunts[1];
      const targetId: string = targetStartedHunt.completeHunt.hunt._id;

      const mockedMethod = spyOn(httpClient, 'get').and.returnValue(of(targetStartedHunt.accessCode));

      startedHuntService.startHunt(targetId).subscribe(() => {
        expect(mockedMethod)
          .withContext('one call')
          .toHaveBeenCalledTimes(1);

        expect(mockedMethod)
          .withContext('talks to the correct endpoint')
          .toHaveBeenCalledWith(`${startedHuntService.startHuntUrl}/${targetId}`);
      });
    }));
  });

  describe('Ending a hunt using `endStartedHunt()`', () => {
    it('calls api/endHunt/id with the correct ID', waitForAsync(() => {
      const targetStartedHunt: StartedHunt = testStartedHunts[1];
      const targetId: string = targetStartedHunt._id;

      const mockedMethod = spyOn(httpClient, 'put').and.returnValue(of(null));

      startedHuntService.endStartedHunt(targetId).subscribe(() => {
        expect(mockedMethod)
          .withContext('one call')
          .toHaveBeenCalledTimes(1);

        expect(mockedMethod)
          .withContext('talks to the correct endpoint')
          .toHaveBeenCalledWith(`${startedHuntService.endHuntUrl}/${targetId}`, null);
      });
    }));
  });

  describe('Getting all ended hunts using `getEndedHunts()`', () => {
    it('calls api/endedHunts', waitForAsync(() => {
      const mockedMethod = spyOn(httpClient, 'get').and.returnValue(of(testStartedHunts));

      startedHuntService.getEndedHunts().subscribe(() => {
        expect(mockedMethod)
          .withContext('one call')
          .toHaveBeenCalledTimes(1);

        expect(mockedMethod)
          .withContext('talks to the correct endpoint')
          .toHaveBeenCalledWith(`${startedHuntService.endedHuntsUrl}`);
      });
    }));
  });

  describe('MockStartedHuntService', () => {
    let service: MockStartedHuntService;

    beforeEach(() => {
      service = new MockStartedHuntService();
    });

    it('should return the correct EndedHunt for a given id', (done: DoneFn) => {
      service.getEndedHuntById(MockStartedHuntService.testStartedHunts[0]._id).subscribe((hunt: StartedHunt) => {
        expect(hunt).toEqual(MockStartedHuntService.testStartedHunts[0]);
        done();
      });
    });

    it('should return the correct StartedHunt for a given id', (done: DoneFn) => {
      service.getStartedHuntById(MockStartedHuntService.testStartedHunts[0]._id).subscribe((hunt: StartedHunt) => {
        expect(hunt).toEqual(MockStartedHuntService.testStartedHunts[0]);
        done();
      });
    });

    it('should return null for an unknown id', (done: DoneFn) => {
      service.getStartedHuntById('unknown').subscribe((hunt: StartedHunt) => {
        expect(hunt).toBeNull();
        done();
      });
    });
  });

});
