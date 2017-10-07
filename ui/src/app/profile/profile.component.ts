import {Component, NgZone, OnDestroy, OnInit} from '@angular/core';
import {ActivatedRoute, Params, Router} from "@angular/router";
import {User} from "../shared/models/user-model";
import {UserService} from "../shared/services/user.service";
import {RecipeService} from "../shared/services/recipe.service";
import {Recipe} from "../shared/models/recipe/recipe";
import {RecipeCategory} from "../shared/models/recipe/recipe-category";
import {RecipeCategoryService} from "../shared/services/recipecategory.service";


@Component({
  selector: 'app-profile',
  templateUrl: './profile.component.html',
  styleUrls: ['./profile.component.css']
})
export class ProfileComponent implements OnInit {

  private user: User;
  private loggedUser: User;
  public fetched: boolean;
  recipes: Recipe[] = [];
  followers: User[] = [];
  following: User[] = [];
  subscribed: boolean;
  categories: RecipeCategory[] = [];
  resultCategories: any[] = [];
  private categoryQuery: string = "";


  constructor(
    private route: ActivatedRoute,
    private userService: UserService,
    private recipeService: RecipeService,
    private recipeCategoryService: RecipeCategoryService,
    private router: Router) {
  }

  ngOnInit() {
    this.route.params
      .subscribe(
      (params: Params) => {
        const id = params['id'];
        this.userService.getUser(id).then(user => {
          this.user = user;
          this.fetched = true;
          this.loggedUser = JSON.parse(localStorage.getItem("user")) as User;
          this.fetchFollowers();
          this.fetchFollowing();
          this.fetchCategories();
        }, () => { this.fetched = true });
      }
    );

    const id = this.route.snapshot.params['id'];
    this.recipeService.getUserRecipes(id).then(recipes =>
      this.recipes = recipes
    );
    this.loggedUser = JSON.parse(localStorage.getItem("user")) as User;
  }

  private subscribe() {
    this.userService.followUser(this.user.id).subscribe((res : User) => {
      this.followers.push(res);
      this.subscribed = true;
    });
  }

  private unSubscribe() {
    this.userService.unFollowUser(this.user.id).subscribe((res : User) => {
      const index = this.followers.map(u => u.id).indexOf(res.id, 0);
      if (index > -1) {
        this.followers.splice(index, 1);
        this.subscribed = false;
      }
    });
  }

  private fetchFollowers() {
    this.userService.getFollowers(this.route.snapshot.params['id']).subscribe((res : User[]) => {
      this.followers = res;
      this.subscribed = (this.followers.map(u => u.id).indexOf(this.loggedUser.id, 0) > -1);
    });
  }

  private fetchFollowing() {
    this.userService.getFollowing(this.route.snapshot.params['id']).subscribe((res : User[]) => {
      this.following = res;
    });
  }

  private followerClickListener(i: number) {
    this.router.navigate([`/usuario/${this.followers[i].id}/perfil`]);
  }

  private followingClickListener(i: number) {
    this.router.navigate([`/usuario/${this.following[i].id}/perfil`]);
  }

  private fetchCategories() {
    this.userService.getCategories(this.route.snapshot.params['id']).subscribe((res : RecipeCategory[]) => {
      this.categories = res;
    });
  }

  private search() {
    if (this.categoryQuery.length == 0) return;
    this.recipeCategoryService.searchCategories(this.categoryQuery).then(res => {
      console.log(res);
      this.resultCategories = res;
    });
  }

  private subscribeToCategory(id: string) {
    console.log(id + ": subscribed");
    this.recipeCategoryService.subscribeToCategory(id).then();
  }

  private unSubscribeToCategory(id: string) {
    console.log(id + ": unSubscribed");
    this.recipeCategoryService.unSubscribeToCategory(id).then();
  }
}
