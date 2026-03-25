/**
 * @author Prithvi Soans
 * @Version 4.0
 */
import java.util.HashMap;
import java.util.Map;

abstract class Room
{
    protected String roomType;
    protected int numberOfBeds;
    protected int squareFeet;
    protected double pricePerNight;

    public Room(String roomType, int numberOfBeds, int squareFeet, double pricePerNight)
    {
        this.roomType = roomType;
        this.numberOfBeds = numberOfBeds;
        this.squareFeet = squareFeet;
        this.pricePerNight = pricePerNight;
    }


    public void displayRoomDetails()
    {
        System.out.println();
        System.out.println(roomType);
        System.out.println("Number of beds: " + numberOfBeds);
        System.out.println("Size: " + squareFeet + " sqft");
        System.out.println("Price per night: " + pricePerNight);
    }
}

class SingleRoom extends Room
{
    SingleRoom()
    {
        super("Single", 1, 250, 4500);
    }

    public void displayRoomDetails()
    {
        super.displayRoomDetails();
    }
}
class DoubleRoom extends Room
{
    DoubleRoom()
    {
        super("Double", 2, 450, 7000);
    }

    public void displayRoomDetails()
    {
        super.displayRoomDetails();
    }
}

class SuiteRoom extends Room
{
    SuiteRoom()
    {
        super("Suite", 3, 650, 4500);
    }

    public void displayRoomDetails()
    {
        super.displayRoomDetails();
    }
}

class RoomInventory
{
    private Map<String, Integer> roomAvailability = new HashMap<>();

    public RoomInventory()
    {
        initialiseInventory("Single", 5);
        initialiseInventory("Double", 3);
        initialiseInventory("Suite", 2);
    }

    private void initialiseInventory(String roomType, int roomCount)
    {
        roomAvailability.put(roomType, roomCount);
    }

    public Map<String, Integer> getRoomAvailability()
    {
        return roomAvailability;
    }

    public void updateAvailability(String roomType, int roomCount)
    {
        roomAvailability.put(roomType, roomCount);
    }
}

class SearchService
{
    public void searchAvailableRooms(RoomInventory inventory, Room singleRoom, Room doubleRoom, Room suiteRoom)
    {
        Map<String, Integer> availability = inventory.getRoomAvailability();

        if(availability.get("Single") > 0)
        {
            singleRoom.displayRoomDetails();
            System.out.println("Available Rooms: " + availability.get("Single"));
        }


        if(availability.get("Double") > 0)
        {
            doubleRoom.displayRoomDetails();
            System.out.println("Available Rooms: " + availability.get("Double"));
        }

        if(availability.get("Suite") > 0)
        {
            suiteRoom.displayRoomDetails();
            System.out.println("Available Rooms: " + availability.get("Suite"));
        }
    }
}

public class UC4SearchCheck
{
    public static void main(String[] args)
    {
        Room r1 = new SingleRoom();
        Room r2 = new DoubleRoom();
        Room r3 = new SuiteRoom();

        RoomInventory inventory = new RoomInventory();

        inventory.updateAvailability("Suite", 0);

        SearchService ss = new SearchService();
        ss.searchAvailableRooms(inventory, r1, r2, r3);

    }

}

