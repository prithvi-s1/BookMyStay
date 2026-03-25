/**
 * @author Prithvi Soans
 * @Version 3.0
 */
import java.util.HashMap;
import java.util.Map;

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

public class UC3InventorySetup
{
    public static void main(String[] args)
    {
        RoomInventory r = new RoomInventory();

        System.out.println("before updating: ");

        System.out.println(r.getRoomAvailability()+ "\n");

        r.updateAvailability("Single", 2);
        r.updateAvailability("Double", 4);
        r.updateAvailability("Double", 1);

        System.out.println("after updating: ");

        System.out.println(r.getRoomAvailability());
    }

}

