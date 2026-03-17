/**
 * @author prithvi soans
 * @version 2.0
 */

abstract class Room
{
    protected String roomType;
    protected int numberOfBeds;
    protected int squareFeet;
    protected double pricePerNight;
    protected int roomsAvailable;

    public Room(String roomType, int numberOfBeds, int squareFeet, double pricePerNight, int roomsAvailable)
    {
        this.roomType = roomType;
        this.numberOfBeds = numberOfBeds;
        this.squareFeet = squareFeet;
        this.pricePerNight = pricePerNight;
        this.roomsAvailable = roomsAvailable;
    }

    public void displayRoomDetails()
    {
        System.out.println(roomType);
        System.out.println("Number of beds: " + numberOfBeds);
        System.out.println("Size: " + squareFeet + " sqft");
        System.out.println("Price per night: " + pricePerNight);
        System.out.println("Rooms available: " + roomsAvailable);
        System.out.println();

    }
}

class singleRoom extends Room
{
    singleRoom()
    {
        super("SINGLE ROOM", 250, 1500, 4500, 4);
    }

    @Override
    public void displayRoomDetails()
    {
        super.displayRoomDetails();
    }
}

class doubleRoom extends Room
{
    doubleRoom()
    {
        super("DOUBLE ROOM", 400, 2500, 3, 2);
    }

    @Override
    public void displayRoomDetails()
    {
        super.displayRoomDetails();
    }
}

class suiteRoom extends Room
{
    suiteRoom()
    {
        super("SUITE ROOM", 3, 800, 4500, 2);
    }

    @Override
    public void displayRoomDetails()
    {
        super.displayRoomDetails();
    }
}

public class UC2RoomInit
{
    public  static  void  main(String[] args)
    {
        singleRoom r1 = new singleRoom();
        r1.displayRoomDetails();

        doubleRoom r2 = new doubleRoom();
        r2.displayRoomDetails();

        suiteRoom r3 = new suiteRoom();
        r3.displayRoomDetails();
    }

}
