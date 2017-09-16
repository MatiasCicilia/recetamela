package controllers.user;

import com.avaje.ebean.Ebean;
import com.google.inject.Inject;
import controllers.BaseController;
import models.FreeUser;
import models.PremiumUser;
import models.User;
import play.data.Form;
import play.data.FormFactory;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Results;
import services.user.PremiumUserService;

import java.rmi.NoSuchObjectException;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class PremiumUserController extends BaseController {

    private static Form<PremiumUser> userForm;

    @Inject
    public PremiumUserController(FormFactory formFactory) {
        userForm =  formFactory.form(PremiumUser.class);
    }

    public Result createPremiumUser() {
        PremiumUser user = userForm.bindFromRequest().get();
        user.setExpirationDate(LocalDate.now().plus(Period.ofMonths(1)));
        user.save();
        return ok(Json.toJson(user));
    }


    public Result upgradePremiumUser(Long id) {
        //Para upgradear a Chef
        return ok();
    }

    public Result downgradePremiumUser(Long id) {
        Optional<PremiumUser> premiumUserOptional = PremiumUserService.getInstance().get(id);
        if (!premiumUserOptional.isPresent()) return notFound();
        PremiumUser user = premiumUserOptional.get();
        FreeUser freeUser = new FreeUser(user.getName(), user.getLastName(), user.getEmail(), user.getProfilePic());
        freeUser.setId(user.getId());
        freeUser.setFacebookId(user.getFacebookId());
        freeUser.setAuthToken(user.getAuthToken());
        // freeUser.setCreditCards(user.getCreditCards());
        Ebean.execute(() -> {
            user.delete();
            freeUser.save();
        });
        return ok(Json.toJson(freeUser));
    }

    public Result checkExpirationDate(Long id) {
        PremiumUser user = (PremiumUser) getRequester();
        if (user.isExpired()) return downgradePremiumUser(id);
        return ok(Json.toJson(false));
    }

    public Result updatePremiumUser(Long id) {
        try {
            return updatePremiumUser(id, user -> {
                user.update();
                return ok(Json.toJson(user));
            });
        } catch (NoSuchObjectException err) {
            return notFound(err.getMessage());
        }
    }

    public Result updatePremiumUserExpirationDate(Long id) {
        Optional<PremiumUser> premiumUserOptional = PremiumUserService.getInstance().get(id);
        if (!premiumUserOptional.isPresent()) return notFound();
        PremiumUser user = premiumUserOptional.get();
        user.setExpirationDate(LocalDate.now().plus(Period.ofMonths(1)));
        user.update();
        return ok(Json.toJson(user));
    }

    public Result getPremiumUser(Long id) {
        final Optional<PremiumUser> user = PremiumUserService.getInstance().get(id);
        return user.map(u -> ok(Json.toJson(u))).orElseGet(Results::notFound);
    }

    public Result getPremiumUsers(){
        List<PremiumUser> premiumUsers = PremiumUserService.getInstance().getFinder().all();
        return ok(Json.toJson(premiumUsers));
    }

    public Result deletePremiumUser(Long id){
        Optional<PremiumUser> user = PremiumUserService.getInstance().get(id);
        if (user.isPresent()){
            user.get().delete();
            return ok();
        }
        return notFound();
    }

    private Result updatePremiumUser(Long id, Function<User, Result> function) throws NoSuchObjectException {
        PremiumUser newPremiumUser = userForm.bindFromRequest().get();
        Optional<PremiumUser> optionalPremiumUser = PremiumUserService.getInstance().get(id);
        PremiumUser oldPremiumUser;
        if(optionalPremiumUser.isPresent()) oldPremiumUser = optionalPremiumUser.get();
        else throw new NoSuchObjectException("The user was not found");
        if (newPremiumUser.getName() != null) oldPremiumUser.setName(newPremiumUser.getName());
        if (newPremiumUser.getLastName() != null) oldPremiumUser.setLastName(newPremiumUser.getLastName());
        if (newPremiumUser.getEmail() != null) oldPremiumUser.setEmail(newPremiumUser.getEmail());
        if (newPremiumUser.getProfilePic() != null) oldPremiumUser.setProfilePic(newPremiumUser.getProfilePic());
        if (newPremiumUser.getAuthToken() != null) oldPremiumUser.setAuthToken(newPremiumUser.getAuthToken());
        if (newPremiumUser.getRecipes() != null) oldPremiumUser.setRecipes(newPremiumUser.getRecipes());
        if (newPremiumUser.getRecipebooks() != null) oldPremiumUser.setRecipebooks(newPremiumUser.getRecipebooks());
        if (newPremiumUser.getExpirationDate() != null) oldPremiumUser.setExpirationDate(newPremiumUser.getExpirationDate());
        // if (newPremiumUser.getCreditCards() != null) oldPremiumUser.setCreditCards(newPremiumUser.getCreditCards());
        oldPremiumUser.setFacebookId(newPremiumUser.getFacebookId());
        return function.apply(oldPremiumUser);
    }
}
