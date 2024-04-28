
// import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
// import { ActivatedRoute } from '@angular/router';
// import { ActivatedRouteStub } from 'src/testing/activated-route-stub';
// import { HttpClientTestingModule } from '@angular/common/http/testing';
// import { MatCardModule } from '@angular/material/card';
// import { RouterTestingModule } from '@angular/router/testing';
// import { CommonModule } from '@angular/common';

// import { SelectTeamComponent } from './select-team.component';
// import { TeamService } from '../teams/team.service';
// import { StartedHuntService } from '../startHunt/startedHunt.service';
// import { MockTeamService } from 'src/testing/team.service.mock';
// import { MockStartedHuntService } from 'src/testing/startedHunt.service.mock';

// describe('SelectTeamComponent', () => {
//   let component: SelectTeamComponent;
//   let fixture: ComponentFixture<SelectTeamComponent>;
//   const mockStartedHuntService = new MockStartedHuntService();
//   const mockTeamService = new MockTeamService();
//   const activatedRoute: ActivatedRouteStub = new ActivatedRouteStub();

//   beforeEach(waitForAsync(() => {
//     activatedRoute.setParamMap({ accessCode: '123456' });
//     TestBed.configureTestingModule({
//       imports: [
//         HttpClientTestingModule,
//         RouterTestingModule,
//         MatCardModule,
//         CommonModule,
//       ],
//       providers: [
//         { provide: ActivatedRoute, useValue: activatedRoute },
//         { provide: StartedHuntService, useValue: mockStartedHuntService },
//         { provide: TeamService, useValue: mockTeamService },
//       ]
//     })
//       .compileComponents();
//   }));

//   beforeEach(() => {
//     fixture = TestBed.createComponent(SelectTeamComponent);
//     component = fixture.componentInstance;
//     fixture.detectChanges();
//   });

//   it('should create the component', () => {
//     expect(component).toBeTruthy();
//   });

//   it('should initialize component properties', () => {
//     expect(component.teams).toBeUndefined();
//     expect(component.startedHuntId).toBeUndefined();
//     expect(component.accessCode).toBeUndefined();
//   });

// });
