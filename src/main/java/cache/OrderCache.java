package cache;

import controllers.OrderController;
import model.Order;
import utils.Config;

import java.util.ArrayList;

//TODO: Build this cache and use it FIX
public class OrderCache {

    //List of orders
    private ArrayList<Order> orders;
    // How long cache should live
    private long ttl;
    //Time cache was created
    private long created;

    public OrderCache (){
        this.ttl = Config.getOrderTtl();
    }

    public ArrayList<Order> getOrders (Boolean forceUpdate) {

        // If we wish to clear cache, we can set force update.
        // Otherwise we look at the age of the cache and figure out if we should update.
        // If the list is empty we also check for new products

        if (forceUpdate || ((this.created + this.ttl) <= (System.currentTimeMillis()/1000L)) || this.orders.isEmpty()) {

            // Gets orders and sets it
            this.orders = OrderController.getOrders();
            // Sets timestamp
            this.created = System.currentTimeMillis() / 1000L;

        }
        //Return list with orders
        return this.orders;
    }

}
