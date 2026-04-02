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

public class UC5BookingQueue
{
    public static void main(String[] args)
    {
        System.out.println("Booking Request Queue");
        BookingRequestQueue bookingQueue = new BookingRequestQueue();

        Reservation r1 = new Reservation("Abhi", "Single");
        Reservation r2 = new Reservation("Subha", "Double");
        Reservation r3 = new Reservation("Vanmathu", "Suite");

        bookingQueue.addRequest(r1);
        bookingQueue.addRequest(r2);
        bookingQueue.addRequest(r3);

        int count = 1;

        while (bookingQueue.hasPendingRequests())
        {
            Reservation current = bookingQueue.getNextRequest();
            System.out.println("Processing Request #" + (count++) +
                    ": Guest: " + current.getGuestName() +
                    " | Room: " + current.getRoomType());
        }
    }
}