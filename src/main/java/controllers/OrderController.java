package controllers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import model.*;
import utils.Log;

import javax.sound.sampled.Line;

public class OrderController {

  private static DatabaseController dbCon;

  public OrderController() {
    dbCon = new DatabaseController();
  }

  public static Order getOrder(int id) {

    // check for connection
    if (dbCon == null) {
      dbCon = new DatabaseController();
    }

    // Build SQL string to query
    String sql = "SELECT * FROM orders\n" +
            "inner join\n" +
            "user ON orders.user_id = user.id\n" +
            "inner join \n" +
            "line_item ON orders.id = line_item.order_id \n" +
            "inner join \n" +
            "address AS ba ON orders.billing_address_id = ba.id\n" +
            "inner join \n" +
            "address as sa ON orders.shipping_address_id = sa.id\n" +
            "inner join \n" +
            "product ON line_item.product_id  = product.id \n" +
            "where orders.id = " + id;

    // Do the query in the database and create an empty object for the results
    ResultSet rs = dbCon.query(sql);
    Order order = null;
    User user = null;
    ArrayList<LineItem> lineItems = new ArrayList<>();
    LineItem lineItem = null;
    Product product = null;
    Address billingAddress = null;
    Address shippingAddress = null;

    try {
      if (rs.next()) {

        // Perhaps we could optimize things a bit here and get rid of nested queries.
        /*User user = UserController.getUser(rs.getInt("user_id"));
        ArrayList<LineItem> lineItems = LineItemController.getLineItemsForOrder(rs.getInt("id"));
        Address billingAddress = AddressController.getAddress(rs.getInt("billing_address_id"));
        Address shippingAddress = AddressController.getAddress(rs.getInt("shipping_address_id")); */

        // Create an object instance of order from the database data

        user =
                new User(
                        rs.getInt("user_id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("password"),
                        rs.getString("email"));

        product =
                new Product(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("sku"),
                        rs.getInt("price"),
                        rs.getString("description"),
                        rs.getInt("stock"));


          lineItems.add(
                  new LineItem(
                          rs.getInt("id"),
                          product,
                          rs.getInt("quantity"),
                          rs.getFloat("price")));


        billingAddress =
                new Address(
                          rs.getInt("ba.id"),
                          rs.getString("ba.name"),
                          rs.getString("ba.street_address"),
                          rs.getString("ba.city"),
                          rs.getString("ba.zipcode")
                );

        shippingAddress =
                new Address(
                        rs.getInt("sa.id"),
                        rs.getString("sa.name"),
                        rs.getString("sa.street_address"),
                        rs.getString("sa.city"),
                        rs.getString("sa.zipcode")
                );

        order =
            new Order(
                rs.getInt("id"),
                user,
                lineItems,
                billingAddress,
                shippingAddress,
                rs.getFloat("order_total"),
                rs.getLong("created_at"),
                rs.getLong("updated_at"));

        // Returns the build order
        return order; 
      } else {
        System.out.println("No order found");
      }
    } catch (SQLException ex) {
      System.out.println(ex.getMessage());
    }

    // Returns null
    return order;
  }

  /**
   * Get all orders in database
   *
   * @return
   */
  public static ArrayList<Order> getOrders() {

    if (dbCon == null) {
      dbCon = new DatabaseController();
    }
    // Orders instead of order in sql statement

    String sql = "SELECT * FROM orders";


    ResultSet rs = dbCon.query(sql);
    ArrayList<Order> orders = new ArrayList<Order>();

    try {
      while(rs.next()) {

        // Perhaps we could optimize things a bit here and get rid of nested queries.
         User user = UserController.getUser(rs.getInt("user_id"));
        ArrayList<LineItem> lineItems = LineItemController.getLineItemsForOrder(rs.getInt("id"));
        Address billingAddress = AddressController.getAddress(rs.getInt("billing_address_id"));
        Address shippingAddress = AddressController.getAddress(rs.getInt("shipping_address_id"));



        // Create an order from the database data
        Order order =
            new Order(
                rs.getInt("id"),
                user,
                lineItems,
                billingAddress,
                shippingAddress,
                rs.getFloat("order_total"),
                rs.getLong("created_at"),
                rs.getLong("updated_at"));

        // Add order to our list
        orders.add(order);

      }
    } catch (SQLException ex) {
      System.out.println(ex.getMessage());
    }

    // return the orders
    return orders;
  }

  public static Order createOrder(Order order) {

    // Write in log that we've reach this step
    Log.writeLog(OrderController.class.getName(), order, "Actually creating a order in DB", 0);

    // Set creation and updated time for order.
    order.setCreatedAt(System.currentTimeMillis() / 1000L);
    order.setUpdatedAt(System.currentTimeMillis() / 1000L);

    // Check for DB Connection
    if (dbCon == null) {
      dbCon = new DatabaseController();
    }

    // Save addresses to database and save them back to initial order instance
    order.setBillingAddress(AddressController.createAddress(order.getBillingAddress()));
    order.setShippingAddress(AddressController.createAddress(order.getShippingAddress()));

    // Save the user to the database and save them back to initial order instance
    order.setCustomer(UserController.createUser(order.getCustomer()));

    // TODO: Enable transactions in order for us to not save the order if somethings fails for some of the other inserts.

    // Insert the product in the DB
    int orderID = dbCon.insert(
        "INSERT INTO orders(user_id, billing_address_id, shipping_address_id, order_total, created_at, updated_at) VALUES("
            + order.getCustomer().getId()
            + ", "
            + order.getBillingAddress().getId()
            + ", "
            + order.getShippingAddress().getId()
            + ", "
            + order.calculateOrderTotal()
            + ", "
            + order.getCreatedAt()
            + ", "
            + order.getUpdatedAt()
            + ")");

    if (orderID != 0) {
      //Update the productid of the product before returning
      order.setId(orderID);
    }

    // Create an empty list in order to go trough items and then save them back with ID
    ArrayList<LineItem> items = new ArrayList<LineItem>();

    // Save line items to database
    for(LineItem item : order.getLineItems()){
      item = LineItemController.createLineItem(item, order.getId());
      items.add(item);
    }

    order.setLineItems(items);

    // Return order
    return order;
  }
}