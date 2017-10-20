package util;

import akka.stream.javadsl.Source;
import controllers.ScalaNotificationService;
import models.Followers;
import models.notification.Notification;
import models.notification.NotificationType;
import models.user.User;
import play.Logger;
import play.libs.EventSource;
import services.NotificationService;
import services.recipe.RecipeCategoryService;
import services.user.FollowerService;
import sun.rmi.runtime.Log;

import java.util.*;
import java.util.stream.Collectors;

import static play.mvc.Results.ok;

/**
 * Created by Matias Cicilia on 22-Sep-17.
 */
public class NotificationManager {
    private static NotificationManager instance;

    private NotificationManager() {
    }

    public static NotificationManager getInstance() {
        if (instance == null) {
            instance = new NotificationManager();
        }
        return instance;
    }

    /**
     * Create and persist a notification from a specific Sender to a specific Receiver.
     * @param sender The User object represeting the actual sender of the notification
     * @param receiverId Id of the User that will receive the notification
     * @param name Title of the notification. This will determine how the client will display the notification
     * @param message Optional message to display on the client
     * @param redirectId Indicates ID of the object view on which the client should be redirected. For example, if
     *                   the notification indicates a new recipe has been created, redirectId should be the ID of the
     *                   new Recipe that has been created.
     */
    public void emitToUser(User sender, Long receiverId, NotificationType name, String message, String redirectId)  {
        Notification notification = new Notification(sender.getId(), sender.getName() + " " + sender.getLastName(), receiverId, sender.getProfilePic(), name, message, redirectId);
        notification.save();
        ScalaNotificationService.sendNotification(receiverId, notification);
    }

    public void notifyFollowers(User sender, NotificationType type, String message, String redirectId) {
        List<User> followers = FollowerService.getInstance()
                .getFollowers(sender.getId())
                .stream()
                .map(Followers::getFollower)
                .collect(Collectors.toList());
        followers.forEach(follower -> {
            emitToUser(sender, follower.getId(), type, message, redirectId);
        });
    }

    public void notifyCategoryFollowers(User sender, NotificationType type, String message, String redirectId) {
        List<User> followers = RecipeCategoryService.getInstance()
                .getFollowers(Long.parseLong(redirectId))
                .stream()
                .filter(e -> !e.equals(sender))
                .collect(Collectors.toList());
        followers.forEach(follower -> {
            emitToUser(sender, follower.getId(), type, message, redirectId);
        });
    }

    /**
     * Deprecated. Use emitToUser with redirectURI method, so client knows where to redirect user after
     * the notification has been clicked. If no redirect URI has been specified, client will default to
     * redirecing the user to the sender profile.
     */
    @Deprecated
    public void emitToUser(User sender, Long receiverId, NotificationType name, String message)  {
        emitToUser(sender, receiverId, name, message, "");
    }


}
