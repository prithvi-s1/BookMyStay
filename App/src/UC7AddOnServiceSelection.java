import java.util.*;
/**
 * @author Prithvi Soans
 * @version 5.0
 */

import java.util.Queue;
import java.util.LinkedList;

class Reservation
{
    private String guestName;
    private String roomType;

    public Reservation(String guestName, String roomType)
    {
        this.guestName = guestName;
        this.roomType =roomType;
    }

    public String getGuestName()
    {
        return guestName;
    }

    public String getRoomType()
    {
        return roomType;
    }
}

class BookingRequestQueue
{
    private Queue<Reservation> requestQueue;

    public BookingRequestQueue()
    {
        requestQueue = new LinkedList<>();
    }

    public void addRequest(Reservation reservation)
    {
        requestQueue.offer(reservation);
    }

    public Reservation getNextRequest()
    {
        return requestQueue.poll();
    }

    public boolean hasPendingRequests()
    {
        return !requestQueue.isEmpty();
    }
}

/**
 * Service to handle room assignment logic and uniqueness enforcement.
 */
class RoomAllocationService {
    private Set<String> allocatedRoomIds;
    private Map<String, Set<String>> assignedRoomsByType;
    private int idCounter = 101; // Simple counter for unique IDs

    public RoomAllocationService() {
        allocatedRoomIds = new HashSet<>();
        assignedRoomsByType = new HashMap<>();
    }

    /**
     * Confirms a booking request by assigning a unique room ID and updating inventory.
     */
    public void allocateRoom(Reservation reservation, Map<String, Integer> inventory) {
        String type = reservation.getRoomType();

        // 1. Check Inventory
        if (inventory.getOrDefault(type, 0) > 0) {
            // 2. Generate Unique ID
            String roomId = generateRoomId(type);

            // 3. Record Allocation (Prevent Reuse)
            allocatedRoomIds.add(roomId);
            assignedRoomsByType.putIfAbsent(type, new HashSet<>());
            assignedRoomsByType.get(type).add(roomId);

            // 4. Decrement Inventory
            inventory.put(type, inventory.get(type) - 1);

            System.out.println("CONFIRMED: " + reservation.getGuestName() +
                    " assigned to " + roomId + " [" + type + "]");
        } else {
            System.out.println("REJECTED: No availability for " + type + " (Guest: " + reservation.getGuestName() + ")");
        }
    }

    private String generateRoomId(String roomType) {
        // Generates ID like "SNG-101", "DBL-102", etc.
        return roomType.substring(0, 3).toUpperCase() + "-" + (idCounter++);
    }
}
/**
 * Represents an individual optional offering.
 */
class AddOnService {
    private String serviceName;
    private double cost;

    public AddOnService(String serviceName, double cost) {
        this.serviceName = serviceName;
        this.cost = cost;
    }

    public String getServiceName() { return serviceName; }
    public double getCost() { return cost; }
}

/**
 * Manages the association between reservations and multiple selected services.
 */
class AddOnServiceManager {
    private Map<String, List<AddOnService>> servicesByReservation;

    public AddOnServiceManager() {
        this.servicesByReservation = new HashMap<>();
    }

    /**
     * Attaches a service to a reservation ID using a One-to-Many mapping.
     */
    public void addService(String reservationId, AddOnService service) {
        // If the ID isn't in the map, create a new list for it
        servicesByReservation.computeIfAbsent(reservationId, k -> new ArrayList<>()).add(service);
        System.out.println("Added Service: " + service.getServiceName() + " to Reservation: " + reservationId);
    }

    /**
     * Aggregates the costs of all services attached to a specific ID.
     */
    public double calculateTotalServiceCost(String reservationId) {
        List<AddOnService> services = servicesByReservation.get(reservationId);
        if (services == null) return 0.0;

        double total = 0.0;
        for (AddOnService service : services) {
            total += service.getCost();
        }
        return total;
    }
}

/**
 * Main Class: UseCase7AddOnServiceSelection
 * Demonstrates business extensibility and cost aggregation.
 */
public class UC7AddOnServiceSelection {
    public static void main(String[] args) {
        System.out.println("Hotel Add-On Service Selection");
        System.out.println("------------------------------");

        // 1. Setup the Manager
        AddOnServiceManager serviceManager = new AddOnServiceManager();

        // 2. Define Available Services
        AddOnService wifi = new AddOnService("Premium WiFi", 15.0);
        AddOnService breakfast = new AddOnService("Buffet Breakfast", 25.0);
        AddOnService spa = new AddOnService("Spa Treatment", 80.0);

        // 3. Simulate Guest Selections for specific Reservation IDs
        String resId1 = "SNG-101"; // From Use Case 6
        String resId2 = "DBL-102";

        // Guest 1 selects WiFi and Breakfast
        serviceManager.addService(resId1, wifi);
        serviceManager.addService(resId1, breakfast);

        // Guest 2 selects Spa and Breakfast
        serviceManager.addService(resId2, spa);
        serviceManager.addService(resId2, breakfast);

        // 4. Output Calculations
        System.out.println("------------------------------");
        System.out.println("Total Add-On Cost for " + resId1 + ": $" + serviceManager.calculateTotalServiceCost(resId1));
        System.out.println("Total Add-On Cost for " + resId2 + ": $" + serviceManager.calculateTotalServiceCost(resId2));

        System.out.println("\nCore booking logic remains untouched. State integrity maintained.");
    }
}