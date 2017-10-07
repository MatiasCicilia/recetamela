import {Component, Input, OnInit} from '@angular/core';
import {RecipeCategory} from "../../shared/models/recipe/recipe-category";
import {RecipeCategoryService} from "../../shared/services/recipecategory.service";
import {User} from "../../shared/models/user-model";

@Component({
  selector: 'app-category-list',
  templateUrl: './category-list.component.html',
  styleUrls: ['./category-list.component.css']
})
export class CategoryListComponent implements OnInit {

  @Input() categories: any[];

  constructor(private recipeCategoryService: RecipeCategoryService) {
  }

  ngOnInit() {
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
