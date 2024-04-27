import { HttpClient } from "@angular/common/http";
import { HttpClientTestingModule, HttpTestingController } from "@angular/common/http/testing";
import { StartedHunt } from "./startedHunt";
import { StartedHuntService } from "./startedHunt.service";
import { Hunt } from "../hunts/hunt";
import { TestBed } from "@angular/core/testing";

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

  const testStartedHunts: StartedHunt[] = [
    {
      _id: 'startedHunt1_id',
      accessCode: '123456',
      completeHunt: {
        hunt: testHunts[0],
        tasks: [
          {
            _id: '5678',
            huntId: 'ann_id',
            name: 'Take a picture of a bird',
            status: false,
            photos: [],
          },
          {
            _id: '8765',
            huntId: 'ann_id',
            name: 'Take a picture of a dog',
            status: false,
            photos: [],
          },
          {
            _id: '1357',
            huntId: 'ann_id',
            name: 'Take a picture of a Stop sign',
            status: false,
            photos: [],
          },
        ],
      },
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

});
