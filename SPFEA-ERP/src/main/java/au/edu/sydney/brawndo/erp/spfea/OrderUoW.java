package au.edu.sydney.brawndo.erp.spfea;

import au.edu.sydney.brawndo.erp.auth.AuthToken;
import au.edu.sydney.brawndo.erp.database.TestDatabase;
import au.edu.sydney.brawndo.erp.ordering.Order;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The OrderUoW class represents the Unit of Work pattern for managing orders and their state.
 * This class mitigates the need to save to DB after every order change and commits changes when user logs out
 */
public class OrderUoW {
    private Map<Order, String> objectState;
    private AuthToken authToken;

    /**
     * Constructs an OrderUoW instance with the given authentication token.
     *
     * @param authToken the authentication token
     */
    public OrderUoW(AuthToken authToken) {
        this.authToken = authToken;
        objectState = new HashMap<>();
    }

    /**
     * Registers a new order.
     *
     * @param order the order to register
     */
    public void registerNew(Order order) {
        objectState.put(order, "new");
    }

    /**
     * Registers a dirty (modified) order.
     *
     * @param order the order to register
     */
    public void registerDirty(Order order) {
        objectState.put(order, "dirty");
    }

    /**
     * Registers a clean (unchanged) order.
     *
     * @param order the order to register
     */
    public void registerClean(Order order) {
        objectState.put(order, "clean");
    }

    /**
     * Registers a deleted order.
     *
     * @param order the order to register
     */
    public void registerDeleted(Order order) {
        objectState.put(order, "deleted");
    }

    /**
     * Commits the changes made to the orders.
     */
    public void commit() {
        for (Map.Entry<Order, String> entry : objectState.entrySet()) {
            Order order = entry.getKey();
            String state = entry.getValue();
            if (state.equals("new")) {
                saveObject(order);
            } else if (state.equals("dirty")) {
                updateObject(order);
            } else if (state.equals("deleted")) {
                deleteObject(order);
            }
        }
        objectState.clear();
    }

    /**
     * Saves a new order to the database.
     *
     * @param order the order to save
     */
    private void saveObject(Order order) {
        TestDatabase.getInstance().saveOrder(authToken, order);
//        objectState.put(order, "clean");
    }

    /**
     * Updates an existing order in the database.
     *
     * @param order the order to update
     */
    private void updateObject(Order order) {
        TestDatabase.getInstance().removeOrder(authToken, order.getOrderID());
        TestDatabase.getInstance().saveOrder(authToken, order);
    }

    /**
     * Deletes an order from the database.
     *
     * @param order the order to delete
     */
    private void deleteObject(Order order) {
        Order dbOrder = TestDatabase.getInstance().getOrder(authToken, order.getOrderID());
        if (dbOrder == null) {
            return; // if order to be deleted doesn't exist in DB because it hasn't been committed, we can just return
        } else {
            TestDatabase.getInstance().removeOrder(authToken, order.getOrderID());
        }
    }

    /**
     * Returns the order with the given ID if it is dirty or new. Used by SPFEAFacade to find uncommitted Orders
     *
     * @param id the ID of the order
     * @return the order with the given ID if it is dirty or new, null otherwise
     */
    public Order getOrder(int id) {
        for (Order order : objectState.keySet()) {
            if (order.getOrderID() == id) {
                if (objectState.get(order).equals("deleted") || objectState.get(order).equals("clean")) {
                    return null;
                } else {
                    return order;
                }
            }
        }
        return null;
    }

    /**
     * Returns a list of IDs for all new orders that are uncommitted. Used for SPFEAFacade.getAllOrders()
     *
     * @return a list of IDs for new orders
     */
    public List<Integer> getNewOrders() {
        ArrayList<Integer> orders = new ArrayList<>();
        for (Order order : objectState.keySet()) {
            if (objectState.get(order).equals("new")) {
                orders.add(order.getOrderID());
            }
        }
        return orders;
    }
}