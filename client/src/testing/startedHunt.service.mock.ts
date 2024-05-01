import { Injectable } from "@angular/core";
import { Observable, of } from "rxjs";
import { AppComponent } from "src/app/app.component";
import { Hunt } from "src/app/hunts/hunt";
import { Task } from "src/app/hunts/task";
import { StartedHunt } from "src/app/startHunt/startedHunt";
import { StartedHuntService } from "src/app/startHunt/startedHunt.service";

@Injectable({
  providedIn: AppComponent,
})
export class MockStartedHuntService extends StartedHuntService {
  static testHunts: Hunt[] = [
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

  static testTasks: Task[] = [
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

  static testStartedHunts: StartedHunt[] = [
    {
      _id: 'startedHunt1_id',
      accessCode: '123456',
      completeHunt: {
        hunt: MockStartedHuntService.testHunts[0],
        tasks: MockStartedHuntService.testTasks
      },
      status: true,
      submissionIds: ['1234', '5432'],
    },
    {
      _id: 'startedHunt2_id',
      accessCode: '654321',
      completeHunt: {
        hunt: MockStartedHuntService.testHunts[1],
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

  constructor() {
    super(null);
  }

  getStartedHunt(accessCode: string): Observable<StartedHunt> {
    if (accessCode === MockStartedHuntService.testStartedHunts[0].accessCode) {
      return of(MockStartedHuntService.testStartedHunts[0]);
    } else if (accessCode === MockStartedHuntService.testStartedHunts[1].accessCode) {
      return of(MockStartedHuntService.testStartedHunts[1]);
    } else {
      return of(null);
    }
  }

  getEndedHunts(): Observable<StartedHunt[]> {
    return of(MockStartedHuntService.testStartedHunts);
  }

  getEndedHuntById(id: string): Observable<StartedHunt> {
    if (id === MockStartedHuntService.testStartedHunts[0]._id) {
      return of(MockStartedHuntService.testStartedHunts[0]);
    }
  }

  getStartedHuntById(id: string): Observable<StartedHunt> {
    console.log('getStartedHuntById called with id:', id);
    if (id === MockStartedHuntService.testStartedHunts[0]._id) {
      return of(MockStartedHuntService.testStartedHunts[0]);
    } else if (id === MockStartedHuntService.testStartedHunts[1]._id) {
      return of(MockStartedHuntService.testStartedHunts[1]);
    } else {
      return of(null);
    }
  }
}
