import { TestBed } from '@angular/core/testing';
import { Subject } from 'rxjs';

import { WebSocketService } from './web-socket.service';

describe('WebSocketService', () => {
  let service: WebSocketService;
  let nextSpy: jasmine.Spy;
  let asObservableSpy: jasmine.Spy;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(WebSocketService);
    nextSpy = spyOn(service['socket$'], 'next');
    asObservableSpy = spyOn(service['socket$'], 'asObservable').and.returnValue(new Subject());
  });

  afterEach(() => {
    nextSpy.calls.reset();
    asObservableSpy.calls.reset();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should send message', () => {
    const data = { test: 'test' };

    service.sendMessage(data);

    expect(nextSpy).toHaveBeenCalledWith(data);
  });

  it('should receive message', () => {
    service.onMessage();

    expect(asObservableSpy).toHaveBeenCalled();
  });
});
