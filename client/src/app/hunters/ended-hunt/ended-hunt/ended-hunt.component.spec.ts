import { ComponentFixture, TestBed } from '@angular/core/testing';

import { EndedHuntComponent } from './ended-hunt.component';

describe('EndedHuntComponent', () => {
  let component: EndedHuntComponent;
  let fixture: ComponentFixture<EndedHuntComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [EndedHuntComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(EndedHuntComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
