import { Injectable } from '@angular/core';
import {User} from "../models/user-model";
import {HttpClient, HttpHeaders} from "@angular/common/http";
import {LoginData} from "../models/login-data";
import {MyAuthService} from "../../auth/my-auth-service";
import {SharedService} from "./shared.service";
import {AuthToken} from "../models/auth-token";
import {Router} from "@angular/router";
import {ToasterService} from "angular2-toaster";
import {CheckExpirationDateResponse} from "../models/ced-response";
import {Observable} from "rxjs/Observable";
import {Notification} from "../models/notification";
import {RecipeCategory} from "../models/recipe/recipe-category";
import {Subject} from "rxjs";

@Injectable()
export class UserService {

  private headers: HttpHeaders = new HttpHeaders({'Content-Type':'application/json'});
  private subject = new Subject<User>();
  constructor(private http:HttpClient, private router: Router, private toaster: ToasterService, private auth: MyAuthService, private sharedService: SharedService) { }

  public registerUser(user: User) {
    const json = JSON.stringify(user);
    this.http.post('/api/user', json, {headers: this.headers}).subscribe(res => console.log(res));
  }

  public sendLoginData(data: LoginData) {
    const json = JSON.stringify(data);
    this.http.post<AuthToken>('/api/auth/login', json, {headers: this.headers}).subscribe(res => {
      const tokenResponse = res;
      console.log('Server accepted login and opened session for userId: ' + tokenResponse.userId);
      this.auth.saveToken(tokenResponse.token);
      this.auth.connectToWs(tokenResponse.userId);
      this.checkExpirationDate(tokenResponse.userId).then(response => {
        localStorage.setItem("user", JSON.stringify(response.user));
        this.sharedService.notifyOther({loggedIn: true});
        if (!response.expired) return;
        this.sharedService.notifyOther({upgradeForm: true, expired: true});
        this.sharedService.notifyOther({premium: false});
      });
    });
  }

  public upgradeFreeUser(id: string) : Promise<User> {
    return this.http.put<User>(`/api/user/${id}/upgradeUser`, {}).toPromise();
  }

  public checkExpirationDate(id: string) : Promise<CheckExpirationDateResponse> {
    return this.http.put<CheckExpirationDateResponse>(`/api/user/${id}/checkDate`, {}).toPromise();
  }

  public getUser(id: string) : Promise<User> {
    return this.http.get<User>(`/api/user/${id}`).toPromise();
  }

  public getModifiedUser(): Observable<User> {
    return this.subject.asObservable();
  }

  public followUser(id: string) : Observable<User> {
    return this.http.post<User>(`/api/user/subscribe/${id}`, "");
  }

  public unFollowUser(id: string) : Observable<User> {
    return this.http.post<User>(`/api/user/unSubscribe/${id}`, "");
  }

  public getFollowers(id: string) : Observable<User[]> {
    return this.http.get<User[]>(`/api/user/followers/${id}`);
  }

  public getFollowing(id: string) : Observable<User[]> {
    return this.http.get<User[]>(`/api/user/following/${id}`);
  }

  public getCategories(id: string) : Observable<RecipeCategory[]> {
    return this.http.get<RecipeCategory[]>(`/api/user/categories/${id}`);
  }

  public getUnfollowedCategories(id: string) : Observable<RecipeCategory[]> {
    return this.http.get<RecipeCategory[]>(`/api/user/categories/unFollowed/${id}`);
  }

  public persistNotification(notification: Notification) : void {
    let notifications: Notification[] = JSON.parse(localStorage.getItem("notifications")) as Notification[];
    if (notifications == null) {
      notifications = [];
    }
    notifications.push(notification);
    localStorage.setItem("notifications", JSON.stringify(notifications));
  }

  public getNotifications() : Notification[] {
    let notifications: Notification[] = JSON.parse(localStorage.getItem("notifications")) as Notification[];
    if (notifications == null) {
      notifications = [];
    }
    return notifications;
  }

  public deleteNotification(i: number) : void {
    let notifications: Notification[] = this.getNotifications();
    notifications.splice(notifications.length -1 -i, 1);
    localStorage.setItem("notifications", JSON.stringify(notifications));
  }

  public modifyUser(id: string, u: User): Promise<User> {
    this.subject.next(u);
    return this.http.put<User>(`/api/user/${id}/modify`, u).toPromise();
  }

  public deleteUser(id: string) : Promise<any> {
    return this.http.delete(`/api/user/${id}/delete`).toPromise();
  }
}
