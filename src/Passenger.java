public class Passenger {

    private Floor currentFloor;
    private int destinationFloor;
    private Direction direction;

    public Passenger(Floor floor) {
        currentFloor = floor;
        setDestinationFloor();
        setDirection();
        pushButton();
    }

    public Passenger(Floor floor, int destinationFloor) {
        currentFloor = floor;
        this.destinationFloor = destinationFloor;
        setDirection();
        pushButton();
    }

    public Floor getCurrentFloor() {
        return currentFloor;
    }

    public int getDestinationFloor() {
        return destinationFloor;
    }

    public void setDestinationFloor() {
        do {
            destinationFloor = RandomGenerator.getRandomNumberInRange(1, Building.numFloors);
        } while (currentFloor.getFloorIndex() == destinationFloor);
    }

    public Direction getDirection() {
        return direction;
    }

    public void setDirection() {
        direction = Direction.getDirection(currentFloor.getFloorIndex(), destinationFloor);
    }

    public void pushButton() {
        currentFloor.pushedButton(direction);
    }
}