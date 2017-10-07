import {ApiService} from "./api-service";
import {HttpClient} from "@angular/common/http";
import {Injectable} from "@angular/core";
import {RecipeCategory} from "../models/recipe/recipe-category";

@Injectable()
export class RecipeCategoryService extends ApiService {

  private URL: string = `${ApiService.API_URL}/recipe`;

  constructor(private http: HttpClient) {
    super();
  }

  create(category: RecipeCategory) : Promise<RecipeCategory> {
    return this.http.post<RecipeCategory>(`${this.URL}/category/create`, category).toPromise();
  }

  update(categoryId: string, category: RecipeCategory) : Promise<RecipeCategory> {
    return this.http.put<RecipeCategory>(`${this.URL}/category/${categoryId}/update`, category).toPromise();
  }

  get(categoryId: string) : Promise<RecipeCategory> {
    return this.http.get<RecipeCategory>(`${this.URL}/category/${categoryId}`).toPromise();
  }

  getAll() : Promise<RecipeCategory[]> {
    return this.http.get<RecipeCategory[]>(`${this.URL}/categories/all`).toPromise();
  }

  delete(categoryId: string) : Promise<any> {
    return this.http.delete<any>(`${this.URL}/category/${categoryId}/delete`).toPromise();
  }

  searchCategories(query: string) : Promise<any[]> {
    return this.http.get<any[]>(`${this.URL}/categories/${query}`).toPromise();
  }

  subscribeToCategory(id: string) {
    return this.http.post<RecipeCategory>(`/api/user/categories/subscribe/${id}`, "").toPromise();
  }

  unSubscribeToCategory(id: string) {
    return this.http.post<RecipeCategory>(`/api/user/categories/unSubscribe/${id}`, "").toPromise();
  }

  // isSubscribeToCategory(id: string) : boolean {
    // return this.http.get<boolean>(`/api/user/categories/isSubscribed/${id}`, "").;
  // }
}
