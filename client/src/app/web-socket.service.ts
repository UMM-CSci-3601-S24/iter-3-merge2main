import { Injectable } from '@angular/core';
import { WebSocketSubject } from 'rxjs/webSocket';

@Injectable({
  providedIn: 'root'
})
export class WebSocketService {
  private socket$: WebSocketSubject<unknown>;

  constructor() {
    this.socket$ = new WebSocketSubject('ws://' + window.location.hostname + ':4567/ws/host');
  }
  // use this when testing 'ws://' + window.location.hostname + ':4567/ws/host'

  sendMessage(data: unknown) {
    this.socket$.next(data);
  }

  onMessage() {
    return this.socket$.asObservable();
  }
}
