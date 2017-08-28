package models.recipe;

import models.BaseModel;

public class RecipeCategory extends BaseModel<RecipeCategory>{

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}