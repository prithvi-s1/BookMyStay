/**
 * @author Prithvi Soans
 * @Version 3.0
 */
import java.util.HashMap;
import java.util.Map;

class RoomInventory
{
    private String roomType;
    private int roomCount;

    private Map<String, Integer> roomAvailability = new HashMap<>();

    public RoomInventory(String roomType, int roomCount)
    {
        this.roomType = roomType;
        this.roomCount = roomCount;
    }

    private void initialiseInventory()
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

}

