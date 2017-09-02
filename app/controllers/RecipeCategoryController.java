package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import models.recipe.RecipeCategory;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import server.error.RequestError;
import services.recipe.RecipeCategoryService;

public class RecipeCategoryController extends Controller {
    
    public Result create() {
        final JsonNode body = request().body().asJson();
        final RecipeCategory category = Json.fromJson(body, RecipeCategory.class);
        if (badCreateRequest(category))
            return badRequest(RequestError.BAD_FORMAT.toString()).as(Http.MimeTypes.JSON);
        category.setName(category.getName().toLowerCase());
        if (RecipeCategoryService.getInstance().existsByName(category.getName()))
            return badRequest(RequestError.CATEGORY_EXISTS.toString()).as(Http.MimeTypes.JSON);
        category.save();
        return ok(Json.toJson(category));
    }

    private boolean badCreateRequest(RecipeCategory category) {
        return
                category == null ||
                category.getName() == null ||
                category.getName().length() == 0;
    }

    public Result get(long id) {
        final RecipeCategory recipeCategory = RecipeCategoryService.getInstance().get(id);
        if (recipeCategory == null) return notFound();
        return ok(Json.toJson(recipeCategory));
    }
}