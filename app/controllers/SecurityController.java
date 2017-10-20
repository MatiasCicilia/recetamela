package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.Parameter;
import com.restfb.Version;
import com.restfb.exception.FacebookException;
import controllers.authentication.Authenticate;
import models.AuthToken;
import models.user.FreeUser;
import models.user.PremiumUser;
import models.user.User;
import models.user.LoginData;
import play.Logger;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import services.LoginService;
import services.user.UserService;
import util.NotificationManager;
import util.ShaUtil;

import java.util.Date;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

/**
 * Created by Matias Cicilia on 10-Sep-17.
 */

public class SecurityController extends Controller {

    public final static String AUTH_TOKEN_HEADER = "Authorization";

    public static User getUser() {
        return (User) Http.Context.current().args.get("user");
    }

    public Result login() {
        /* Me llega data, yo con esa data busco al usuario en mi base de datos. */
        LoginData loginData = Json.fromJson(request().body().asJson(), LoginData.class);
        Optional<User> userOptional = UserService.getInstance().findByFacebookId(loginData.getId());
        String retrievedId;

        try {
        /* Con el accessToken que el usuario dice que recibió de facebook, busco su ID de facebook con FB Graph */
            FacebookClient client = new DefaultFacebookClient(loginData.getAccessToken(), Version.VERSION_2_8);
            retrievedId = fetchFacebookId(client);
        } catch (FacebookException e) {
            e.printStackTrace();
            Logger.debug("An error occurred retrieving FB object");
            return unauthorized();
        }

        /* If User's been found, then check his facebook token and log him in */
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            /* If the accessToken the user claims facebook gave him checks out, then we can log him in. */
            if ( Long.toString(user.getFacebookId()).equals(retrievedId) ) {
                JsonNode resp = generateAuthToken(user);
                return ok(resp);
            }
            else return unauthorized();
        } else {
            /* If we can't match this facebook ID with a user in our database, then this user is Signing Up for the first time */
            return ok(signUp(loginData));
        }
    }

    @Authenticate({FreeUser.class, PremiumUser.class})
    public Result getLoggedData() {
        User me = getUser();
        return ok(Json.toJson(me));
    }

    /* If User's been validated with facebook API, but hasn't been registered, then add him to DB */
    private JsonNode signUp(LoginData loginData) {
        User newUser = new FreeUser();
        newUser.setEmail(loginData.getEmail());
        newUser.setFacebookId(loginData.getId());
        newUser.setName(loginData.getName());
        newUser.setLastName(loginData.getLastName());
        newUser.setProfilePic(loginData.getUrl());
        newUser.save();
        /* After used is saved, log him in */
        return generateAuthToken(newUser);
    }

    private JsonNode generateAuthToken(User user) {
        Date date = new Date();
        String hash = ShaUtil.sha256(user.getName() + date.getTime());
        AuthToken token = new AuthToken(hash, date.getTime(), user.getId());
        user.setAuthToken(token.getToken());
        user.update();
        token.save();
        Logger.debug("Logged in user : " + user.getName() + " with token " + token.getToken());
        LoginService.getInstance().clearOldTokens(user.getId());
        return Json.toJson(token);
    }

    /* Logging out simply consists on deleting given server Authentication token */
    @Authenticate({FreeUser.class, PremiumUser.class})
    public Result logout() throws ExecutionException, InterruptedException {
        Logger.debug("User "+ getUser().getName() + " logged out");
        User user = getUser();

        Optional<AuthToken> tokenOptional = LoginService.getInstance().findByHash(user.getAuthToken());
        tokenOptional.ifPresent(authToken -> authToken.setValid(false));
        tokenOptional.ifPresent(AuthToken::update);
        user.setAuthToken(null);
        user.update();

        return ok();
    }

    private String fetchFacebookId(FacebookClient client) {
        String retrieved = client.fetchObject("me", String.class, Parameter.with("fields", "id"));
        return retrieved.split("\"")[3];
    }
}