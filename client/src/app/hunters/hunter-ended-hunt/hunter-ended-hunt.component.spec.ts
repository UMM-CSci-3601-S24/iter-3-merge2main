import { ComponentFixture, TestBed } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { HunterEndedHuntComponent } from './hunter-ended-hunt.component';

describe('HunterEndedHuntComponent', () => {
  let component: HunterEndedHuntComponent;
  let fixture: ComponentFixture<HunterEndedHuntComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RouterTestingModule, HunterEndedHuntComponent]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(HunterEndedHuntComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
