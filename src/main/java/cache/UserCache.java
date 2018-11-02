package cache;

import com.sun.org.apache.xpath.internal.operations.Bool;
import controllers.UserController;
import model.User;
import utils.Config;

import java.util.ArrayList;

//TODO: Build this cache and use it FIX
public class UserCache {

    private ArrayList<User> users;

    private long ttl;

    private long created;

    public UserCache () {
        this.ttl = Config.getUserTtl();
    }

    public ArrayList<User> getUsers (Boolean forceUpdate){

        if (forceUpdate || ((this.ttl + this.created) <= (System.currentTimeMillis() / 1000L))|| this.users == null) {

            this.users = UserController.getUsers();

            this.created = System.currentTimeMillis() / 1000L;
        }
        return this.users;
    }

}
