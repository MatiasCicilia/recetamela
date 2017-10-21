import { Injectable } from '@angular/core';
import {Observable} from "rxjs/Observable";

@Injectable()
export class WebSocketService {

  private socket;

  constructor() { }

  public connect(id: string) {
    if(!this.socket) {
      this.socket = this.create(id);
    }
    return this.socket;
  }

  public send(data: string) {
    let myMessage = new MessageEvent('message', {
      data : data
    });
    this.socket.next(JSON.stringify(myMessage.data));
  }

  private create(id: string) {
    let subject = Observable.webSocket(`ws://localhost:9000/api/ws/notifications/${id}`).share();
    subject.subscribe();
    return subject;
  }

}
