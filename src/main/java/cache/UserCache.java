package cache;

import com.sun.org.apache.xpath.internal.operations.Bool;
import controllers.UserController;
import model.User;
import utils.Config;

import java.util.ArrayList;

//TODO: Build this cache and use it FIX
public class UserCache {

    //List of users
    private ArrayList<User> users;
    // Time cache should live
    private long ttl;
    //Time cache created
    private long created;

    public UserCache () {
        this.ttl = Config.getUserTtl();
    }

    // If we wish to clear cache, we can set force update.
    // Otherwise we look at the age of the cache and figure out if we should update.
    // If the list is empty we also check for new products

    public ArrayList<User> getUsers (Boolean forceUpdate){

        if (forceUpdate || ((this.ttl + this.created) <= (System.currentTimeMillis() / 1000L))|| this.users == null) {

            // Get products from controller, since we wish to update and sets it
            this.users = UserController.getUsers();

            // Set created timestamp
            this.created = System.currentTimeMillis() / 1000L;
        }
        // Returns list with users
        return this.users;
    }

}
