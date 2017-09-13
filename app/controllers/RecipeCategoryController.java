package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import models.recipe.RecipeCategory;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Results;
import server.error.RequestError;
import services.recipe.RecipeCategoryService;

import java.util.List;
import java.util.Optional;

public class RecipeCategoryController extends Controller {

    // @Authenticate({FreeUser.class, PremiumUser.class})
    public Result create() {
        final JsonNode body = request().body().asJson();
        final RecipeCategory category = Json.fromJson(body, RecipeCategory.class);
        if (badCreateRequest(category))
            return badRequest(RequestError.BAD_FORMAT.toString()).as(Http.MimeTypes.JSON);
        category.setName(category.getName().toLowerCase());
        if (RecipeCategoryService.getInstance().getByName(category.getName()).isPresent())
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

    // @Authenticate({FreeUser.class, PremiumUser.class})
    public Result get(long id) {
        final Optional<RecipeCategory> category = RecipeCategoryService.getInstance().get(id);
        return category.map(c -> ok(Json.toJson(c))).orElseGet(Results::notFound);
    }

    // @Authenticate({FreeUser.class, PremiumUser.class})
    public Result getAll() {
        final List<RecipeCategory> categories = RecipeCategoryService.getInstance().getFinder().all();
        return ok(Json.toJson(categories));
    }
}
