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
    private Map<Long, Queue<EventOutput>> events;

    private NotificationManager() {
        events = new HashMap<>();
    }

    public static NotificationManager getInstance() {
        if (instance == null) {
            instance = new NotificationManager();
        }
        return instance;
    }

    public void subscribe(long userId) {
        /* Levanto de la DB todas las notificaciones que se perdio */
        EventOutput output = fetchMissedEvents(userId);
        Queue<EventOutput> eventsQueue = new LinkedList<>();
        if (output.updated) eventsQueue.add(output);
        events.put(userId, eventsQueue);
    }

    public EventOutput requestNotifications(long userId) {
        Queue<EventOutput> queue = events.get(userId);
        if (queue != null && !queue.isEmpty()) {
            return queue.poll();
        }
        return null;
    }

    public void unsubscribe(long userId) {
        events.remove(userId);
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
        Queue<EventOutput> queue = events.get(receiverId);
        ScalaNotificationService.sendNotification(receiverId, notification);
        if (queue != null) {
            EventOutput event = new EventOutput();
            event.source = Source.single(notification.toEvent());
            event.updated = true;
            queue.add(event);;
            //notification.setDelivered(true);
        }
        notification.save();
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

    private EventOutput fetchMissedEvents(long userId) {
        List<Notification> missedEvents = NotificationService.getInstance().getUndeliveredByUser(userId);

        /* Armo mi stream de eventos con las notificaciones */
        EventOutput eventOutput = new EventOutput();
        eventOutput.updated = !missedEvents.isEmpty();
        eventOutput.source = Source.from(missedEvents.stream().map(Notification::toEvent).collect(Collectors.toList()));
        missedEvents.forEach(e -> {
            e.setDelivered(true);
            e.update();
        });
        return eventOutput;
    }

    public class EventOutput {
        private Source<EventSource.Event, ?> source;
        private boolean updated;

        public Source<EventSource.Event, ?> getSource() {
            return source;
        }

        public void setSource(Source<EventSource.Event, ?> source) {
            this.source = source;
        }

        public boolean isUpdated() {
            return updated;
        }

        public void setUpdated(boolean updated) {
            this.updated = updated;
        }
    }


}
