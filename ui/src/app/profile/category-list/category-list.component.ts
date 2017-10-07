import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {RecipeCategoryService} from "../../shared/services/recipecategory.service";

@Component({
  selector: 'app-category-list',
  templateUrl: './category-list.component.html',
  styleUrls: ['./category-list.component.css']
})
export class CategoryListComponent implements OnInit {


  @Input() categories: any[];
  @Output() subscribed: EventEmitter<any[]> = new EventEmitter<any[]>();
  private subscribedCategories: any[] = [];

  constructor(private recipeCategoryService: RecipeCategoryService) {
  }

  ngOnInit() {
  }

  subscribe(index: number): void {
    const cat = this.categories[index];
    if (cat.followed) {
      this.unSubscribeToCategory(cat.category.id);
    } else {
      this.subscribeToCategory(cat.category.id);
      this.subscribedCategories.push(cat.category);
      this.subscribed.emit(this.subscribedCategories);
    }
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
