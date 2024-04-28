import { TestBed } from '@angular/core/testing';
import { HuntCardComponent } from './hunt-card.component';
import { HostService } from '../hosts/host.service';
import { Router } from '@angular/router';
import { of } from 'rxjs';

describe('HuntCardComponent', () => {
  let component: HuntCardComponent;
  let hostService: HostService;
  let router: Router;

  beforeEach(() => {
    hostService = jasmine.createSpyObj('HostService', ['startHunt']);
    router = jasmine.createSpyObj('Router', ['navigate']);

    TestBed.configureTestingModule({
      providers: [
        HuntCardComponent,
        { provide: HostService, useValue: hostService },
        { provide: Router, useValue: router }
      ]
    });

    component = TestBed.inject(HuntCardComponent);
  });

  it('should call hostService.startHunt and router.navigate when startHunt is called', () => {
    const id = 'testId';
    const accessCode = 'accessCode';
    (hostService.startHunt as jasmine.Spy).and.returnValue(of(accessCode));

    component.startHunt(id);

    expect(hostService.startHunt).toHaveBeenCalledWith(id);
    expect(router.navigate).toHaveBeenCalledWith(['/startedHunts/', accessCode, 'addTeams']);
  });
});
