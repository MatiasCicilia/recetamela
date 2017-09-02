package services.recipe;

import com.avaje.ebean.Model.Finder;
import models.recipe.RecipeCategory;
import server.error.RequestError;
import server.exception.BadRequestException;
import services.Service;

public class RecipeCategoryService extends Service<RecipeCategory> {
    private static RecipeCategoryService instance;

    private RecipeCategoryService(Finder<Long, RecipeCategory> finder) {
        super(finder);
    }

    public static RecipeCategoryService getInstance() {
        if (instance == null)
            instance = new RecipeCategoryService(new Finder<>(RecipeCategory.class));
        return instance;
    }

    /**
     * Get category by name.
     * @param name Name of the category.
     * @return RecipeCategory if found. If there's more than one category with name an exception
     *         and null if category was not found.
     */
    public RecipeCategory getByName(String name){
        return finder.where().eq("name", name).findUnique();
    }

    public boolean existsByName(String name) {
        return getByName(name) != null;
    }
}
