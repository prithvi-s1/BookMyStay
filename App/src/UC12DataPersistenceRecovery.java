import java.util.*;
import java.io.*;
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
 * Service to handle the translation between in-memory Objects and Disk Storage.
 * Implements simple text-based serialization for transparency.
 */
class FilePersistenceService {

    /**
     * Serializes room inventory state to a file.
     * Format: roomType:availableCount
     */
    public void saveInventory(Map<String, Integer> inventory, String filePath) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            for (Map.Entry<String, Integer> entry : inventory.entrySet()) {
                writer.println(entry.getKey() + ":" + entry.getValue());
            }
            System.out.println("System: Inventory snapshot saved to " + filePath);
        } catch (IOException e) {
            System.err.println("Error saving inventory: " + e.getMessage());
        }
    }

    /**
     * Deserializes room inventory from a file back into the application state.
     */
    public void loadInventory(Map<String, Integer> inventory, String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            System.out.println("System: No persistence file found. Starting with default state.");
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            inventory.clear(); // Clear current memory to reflect the "Last Known State"
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts.length == 2) {
                    inventory.put(parts[0], Integer.parseInt(parts[1]));
                }
            }
            System.out.println("System: State recovered successfully from " + filePath);
        } catch (IOException | NumberFormatException e) {
            System.err.println("Error loading inventory: " + e.getMessage() + ". Starting fresh.");
        }
    }
}

/**
 * Main Class: UseCase12DataPersistenceRecovery
 * Demonstrates the full lifecycle: Operations -> Save -> Restart -> Recover.
 */
public class UC12DataPersistenceRecovery {
    public static void main(String[] args) {
        String storagePath = "hotel_state.txt";
        FilePersistenceService persistence = new FilePersistenceService();
        Map<String, Integer> currentInventory = new HashMap<>();

        // --- PHASE 1: SYSTEM RECOVERY (Startup) ---
        System.out.println("--- Booting Hotel Management System ---");
        persistence.loadInventory(currentInventory, storagePath);

        // If loading failed/empty, initialize defaults
        if (currentInventory.isEmpty()) {
            currentInventory.put("Single", 5);
            currentInventory.put("Suite", 2);
        }
        System.out.println("Current Available Rooms: " + currentInventory);

        // --- PHASE 2: BUSINESS OPERATIONS ---
        System.out.println("\nProcessing a new booking...");
        if (currentInventory.get("Single") > 0) {
            currentInventory.put("Single", currentInventory.get("Single") - 1);
            System.out.println("Booking confirmed. Remaining Singles: " + currentInventory.get("Single"));
        }

        // --- PHASE 3: STATE PERSISTENCE (Shutdown) ---
        System.out.println("\n--- Shutting Down System ---");
        persistence.saveInventory(currentInventory, storagePath);

        System.out.println("Process finished. Restart the program to see data persist!");
    }
}