package services.user;

import com.avaje.ebean.Model;
import models.user.User;
import services.Service;

import java.util.Optional;

public class UserService extends Service<User> {
    private static UserService instance;

    private UserService(Model.Finder<Long, User> finder) {
        super(finder);
    }

    public static UserService getInstance() {
        if (instance == null)
            instance = new UserService(new Model.Finder<>(User.class));
        return instance;
    }

    public Optional<User> findByFacebookId(Long id) {
        return Optional.ofNullable(getFinder().where().eq("facebook_id", id).findUnique());
    }

    public Optional<User> findByAuthToken(String authToken) {
        return Optional.ofNullable(getFinder().where().eq("auth_token", authToken).findUnique());
    }
}
