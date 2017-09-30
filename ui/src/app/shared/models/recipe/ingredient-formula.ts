import {Ingredient} from "./ingredient";

export class IngredientFormula {
  ingredient: Ingredient;
  quantity: string = '';
  unit: string = '';

  constructor(i: Ingredient) {
    this.ingredient = i;
  }
}
