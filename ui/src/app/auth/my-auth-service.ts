import {Injectable} from "@angular/core";
import {HttpClient} from "@angular/common/http";
import {User} from "../shared/models/user-model";
import {SharedService} from "../shared/services/shared.service";
import {Router} from "@angular/router";
import {WebSocketService} from "../shared/services/web-socket.service";

@Injectable()
export class MyAuthService {

  constructor(private http: HttpClient,
              private sharedService: SharedService,
              private router: Router,
              private wsService: WebSocketService) {
  }

  /* Once logged out we delete the server-signed token from our local storage */
  public logout() {
    this.http.post('/api/auth/logout', "logout").subscribe(res => {
      localStorage.removeItem("X-TOKEN");
      localStorage.removeItem("user");
      localStorage.removeItem("notifications");
      this.sharedService.notifyOther({loggedIn: false});
      this.router.navigate(['/']);
    })
  }

  public requestLoggedUser(): Promise<User> {
    return this.http.get<User>('/api/auth/logged-data').toPromise();
  }

  isLoggedIn(): boolean {
    return localStorage.getItem("X-TOKEN") !== null;
  }

  public getAuthorizationHeader(): string {
    return localStorage.getItem("X-TOKEN");
  }

  public saveToken(token: string) {
    localStorage.setItem("X-TOKEN", token);
  }
}
