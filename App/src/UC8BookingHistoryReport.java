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
 * Acts as an in-memory audit log for all confirmed bookings.
 * Preserves the chronological order of transactions.
 */
class BookingHistory {
    private List<Reservation> confirmedReservations;

    public BookingHistory() {
        this.confirmedReservations = new ArrayList<>();
    }

    /**
     * Appends a confirmed reservation to the history.
     */
    public void addReservation(Reservation reservation) {
        confirmedReservations.add(reservation);
    }

    /**
     * Returns a read-only view or a copy to maintain encapsulation.
     */
    public List<Reservation> getConfirmedReservations() {
        return Collections.unmodifiableList(confirmedReservations);
    }
}

/**
 * Separates the logic of data analysis from the data storage itself.
 */
class BookingReportService {
    /**
     * Generates a structured summary of the booking history for Administrators.
     */
    public void generateReport(BookingHistory history) {
        List<Reservation> records = history.getConfirmedReservations();

        System.out.println("\n--- ADMINISTRATIVE BOOKING REPORT ---");
        System.out.println("Total Bookings Confirmed: " + records.size());
        System.out.println("-------------------------------------");
        System.out.printf("%-5s | %-15s | %-10s%n", "No.", "Guest Name", "Room Type");
        System.out.println("-------------------------------------");

        int count = 1;
        for (Reservation res : records) {
            System.out.printf("%-5d | %-15s | %-10s%n",
                    count++, res.getGuestName(), res.getRoomType());
        }
        System.out.println("-------------------------------------\n");
    }
}

/**
 * Main Class: UseCase8BookingHistoryReport
 * Demonstrates the transition from active processing to historical auditing.
 */
public class UC8BookingHistoryReport {
    public static void main(String[] args) {
        // 1. Initialize our Infrastructure
        BookingHistory history = new BookingHistory();
        BookingReportService reportService = new BookingReportService();

        // 2. Simulate the Booking & Confirmation Flow
        // (In a real app, these come from the Queue and Allocation Service)
        Reservation res1 = new Reservation("Abhi", "Single");
        Reservation res2 = new Reservation("Subha", "Double");
        Reservation res3 = new Reservation("Vanmathu", "Suite");

        // 3. Persist successful transactions
        history.addReservation(res1);
        history.addReservation(res2);
        history.addReservation(res3);

        System.out.println("System: Transactions successfully persisted to History.");

        // 4. Admin requests the report
        reportService.generateReport(history);
    }
}