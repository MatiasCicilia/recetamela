package services;

import com.avaje.ebean.Model;
import models.notification.Notification;

import java.util.List;

/**
 * Created by Matias Cicilia on 23-Sep-17.
 */
public class NotificationService extends Service<Notification> {

    private static NotificationService instance;

    public NotificationService(Model.Finder<Long, Notification> finder){
        super(finder);
    }

    public NotificationService() {
        super(new Model.Finder<>(Notification.class));
    }

    public static NotificationService getInstance() {
        if (instance == null) instance = new NotificationService(new Model.Finder<>(Notification.class));
        return instance;
    }

    public List<Notification> findByUser(Long id) {
        return getFinder().where().eq("receiver", id).findList();
    }

    public List<Notification> getUndeliveredByUser(Long id) {
        return getFinder().where()
                .eq("receiver", id)
                .eq("delivered", false)
                .findList();
    }

    public Notification getNullable(Long id) {
        return getFinder().byId(id);
    }
}
