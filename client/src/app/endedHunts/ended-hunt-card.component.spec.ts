import { TestBed } from "@angular/core/testing";
import { Router } from "@angular/router";
import { of } from "rxjs";
import { HostService } from "../hosts/host.service";
import { EndedHuntCardComponent } from "./ended-hunt-card.component";
import { StartedHuntService } from "../startHunt/startedHunt.service";

describe('EndedHuntCardComponent', () => {
  let component: EndedHuntCardComponent;
  let hostService: HostService;
  let startedHuntService: StartedHuntService;
  let router: Router;

  beforeEach(() => {
    hostService = jasmine.createSpyObj('HostService', ['deleteEndedHunt']);
    router = jasmine.createSpyObj('Router', ['navigate']);
    startedHuntService = jasmine.createSpyObj('StartedHuntService', ['deleteStartedHunt']);

    TestBed.configureTestingModule({
      providers: [
        EndedHuntCardComponent,
        { provide: HostService, useValue: hostService },
        { provide: Router, useValue: router },
        { provide: StartedHuntService, useValue: startedHuntService}
      ]
    });

    component = TestBed.inject(EndedHuntCardComponent);
  });

  it('should call hostService.deleteEndedHunt and huntDeleted.emit when deleteEndedHunt is called', () => {
    const id = 'testId';
    (startedHuntService.deleteStartedHunt as jasmine.Spy).and.returnValue(of(undefined));

    spyOn(window, 'confirm').and.returnValue(true); // Add this line

    // Create a spy for huntDeleted.emit
    spyOn(component.huntDeleted, 'emit');

    component.deleteEndedHunt(id);

    expect(startedHuntService.deleteStartedHunt).toHaveBeenCalledWith(id);
    expect(component.huntDeleted.emit).toHaveBeenCalledWith(id);
  });
});
