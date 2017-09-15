import {Injectable} from "@angular/core";
import {HttpClient} from "@angular/common/http";
import {User} from "../shared/models/user-model";
import {SharedService} from "../shared/services/shared.service";
import {Router} from "@angular/router";
import {Observable} from "rxjs/Observable";

@Injectable()
export class MyAuthService {

  constructor(private http: HttpClient, private sharedService: SharedService, private router: Router) {
  }

  /* Once logged out we delete the server-signed token from our local storage */
  public logout() {
    this.http.post('/api/auth/logout', "logout").subscribe(res => {
      localStorage.removeItem("X-TOKEN");
      localStorage.removeItem("user");
      this.sharedService.notifyOther({loggedIn: false});
      this.router.navigate(['/']);
    })
  }

  public requestLoggedUser(): Promise<User> {
    return this.http.get<User>('/api/auth/logged-data').toPromise();
  }

  isLoggedIn(): boolean {
    console.log('User is logged in: ' + (localStorage.getItem("X-TOKEN") !== null));
    return localStorage.getItem("X-TOKEN") !== null;
  }

  public getAuthorizationHeader(): string {
    return localStorage.getItem("X-TOKEN");
  }

  public saveToken(token: string) {
    localStorage.setItem("X-TOKEN", token);
  }
}
