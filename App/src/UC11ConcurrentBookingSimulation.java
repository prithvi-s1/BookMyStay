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
 * Custom Exception for domain-specific booking failures.
 */
class InvalidBookingException extends Exception {
    public InvalidBookingException(String message) {
        super(message);
    }
}

/**
 * Logic to ensure system constraints are met before any state change occurs.
 */
class ReservationValidator {
    /**
     * Validates input and system state.
     * Checks for empty names, unsupported room types, and zero inventory.
     */
    public void validate(String guestName, String roomType, Map<String, Integer> inventory)
            throws InvalidBookingException {

        // 1. Basic Input Validation
        if (guestName == null || guestName.trim().isEmpty()) {
            throw new InvalidBookingException("Guest name cannot be empty.");
        }

        // 2. Room Type Validation
        if (!inventory.containsKey(roomType)) {
            throw new InvalidBookingException("Room type '" + roomType + "' is not supported by this hotel.");
        }

        // 3. Inventory Constraint Validation
        if (inventory.get(roomType) <= 0) {
            throw new InvalidBookingException("No availability for " + roomType + " rooms.");
        }

        System.out.println("Validation Successful for: " + guestName);
    }
}
/**
 * Service to handle the safe reversal of confirmed bookings.
 * Uses a Stack to track the chronological order of released resources.
 */
class CancellationService {
    // LIFO structure to track released room IDs
    private Stack<String> releasedRoomIds;
    // Internal registry to map IDs back to their categories for inventory restoration
    private Map<String, String> reservationRoomTypeMap;

    public CancellationService() {
        this.releasedRoomIds = new Stack<>();
        this.reservationRoomTypeMap = new HashMap<>();
    }

    /**
     * Registers a confirmed booking so the system knows how to undo it later.
     */
    public void registerBooking(String reservationId, String roomType) {
        reservationRoomTypeMap.put(reservationId, roomType);
    }

    /**
     * Validates and executes a cancellation, restoring inventory counts.
     */
    public void cancelBooking(String reservationId, Map<String, Integer> inventory) {
        // 1. Validation: Ensure the reservation exists
        if (!reservationRoomTypeMap.containsKey(reservationId)) {
            System.out.println("CANCELLATION FAILED: Reservation ID " + reservationId + " not found.");
            return;
        }

        // 2. Identify the room type to restore
        String roomType = reservationRoomTypeMap.get(reservationId);

        // 3. Rollback Logic (LIFO)
        releasedRoomIds.push(reservationId);

        // 4. Update Inventory
        inventory.put(roomType, inventory.get(roomType) + 1);

        // 5. Cleanup Registry
        reservationRoomTypeMap.remove(reservationId);

        System.out.println("SUCCESS: Cancelled " + reservationId + ". " + roomType + " inventory incremented.");
    }

    /**
     * Visualizes the order of rollbacks (Last Cancelled is Top of Stack).
     */
    public void showRollbackHistory() {
        System.out.println("\n--- ROLLBACK HISTORY (LIFO) ---");
        if (releasedRoomIds.isEmpty()) {
            System.out.println("No cancellations recorded.");
        } else {
            // Using a temporary stack or list to show order without destroying the stack
            List<String> history = new ArrayList<>(releasedRoomIds);
            Collections.reverse(history);
            for (String id : history) {
                System.out.println("Released: " + id);
            }
        }
        System.out.println("-------------------------------\n");
    }
}

/**
 * Runnable task that processes bookings from a shared queue.
 * Implements synchronization to prevent race conditions.
 */
class ConcurrentBookingProcessor implements Runnable {
    private final BookingRequestQueue bookingQueue;
    private final Map<String, Integer> inventory;
    private final RoomAllocationService allocationService;

    public ConcurrentBookingProcessor(
            BookingRequestQueue bookingQueue,
            Map<String, Integer> inventory,
            RoomAllocationService allocationService) {
        this.bookingQueue = bookingQueue;
        this.inventory = inventory;
        this.allocationService = allocationService;
    }

    @Override
    public void run() {
        while (true) {
            Reservation reservation = null;

            // 1. Critical Section: Synchronized polling from the shared queue
            synchronized (bookingQueue) {
                if (bookingQueue.hasPendingRequests()) {
                    reservation = bookingQueue.getNextRequest();
                } else {
                    break; // Exit loop if no more requests
                }
            }

            // 2. Critical Section: Synchronized Room Allocation
            // This prevents "Double Booking" if two threads check inventory simultaneously.
            if (reservation != null) {
                synchronized (inventory) {
                    allocationService.allocateRoom(reservation, inventory);
                }
            }

            // Artificial delay to simulate processing time and increase chance of interleaving
            try { Thread.sleep(100); } catch (InterruptedException e) { break; }
        }
    }
}

/**
 * Main Class: UseCase11ConcurrentBookingSimulation
 * Demonstrates thread-safe processing using multiple threads.
 */
public class UC11ConcurrentBookingSimulation {
    public static void main(String[] args) {
        System.out.println("--- Concurrent Booking Simulation (Thread Safety) ---");

        // Shared Resources
        BookingRequestQueue sharedQueue = new BookingRequestQueue();
        Map<String, Integer> sharedInventory = new HashMap<>();
        sharedInventory.put("Suite", 2); // Only 2 suites available

        RoomAllocationService allocationService = new RoomAllocationService();

        // Load the queue with multiple requests for the same limited resource
        sharedQueue.addRequest(new Reservation("Thread-User-1", "Suite"));
        sharedQueue.addRequest(new Reservation("Thread-User-2", "Suite"));
        sharedQueue.addRequest(new Reservation("Thread-User-3", "Suite")); // Should be rejected
        sharedQueue.addRequest(new Reservation("Thread-User-4", "Suite")); // Should be rejected

        // Create multiple worker threads (Simulating concurrent processors)
        Thread t1 = new Thread(new ConcurrentBookingProcessor(sharedQueue, sharedInventory, allocationService), "Processor-1");
        Thread t2 = new Thread(new ConcurrentBookingProcessor(sharedQueue, sharedInventory, allocationService), "Processor-2");

        System.out.println("Starting concurrent processors...");
        t1.start();
        t2.start();

        try {
            // Wait for both threads to finish
            t1.join();
            t2.join();
        } catch (InterruptedException e) {
            System.out.println("Main thread interrupted.");
        }

        System.out.println("-----------------------------------------------------");
        System.out.println("Final Inventory State: " + sharedInventory);
        System.out.println("Simulation complete. All critical sections protected.");
    }
}