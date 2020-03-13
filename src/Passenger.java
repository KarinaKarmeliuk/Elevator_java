public class Passenger {

    private Floor currentFloor;
    private int destinationFloorIndex;
    private Direction direction;

    public Passenger(Floor floor) {
        currentFloor = floor;
        setDestinationFloorIndex();
        setDirection();
    }

    public Floor getCurrentFloor() {
        return currentFloor;
    }

    public int getDestinationFloorIndex() {
        return destinationFloorIndex;
    }

    public void setDestinationFloorIndex() {
        do {
            destinationFloorIndex = RandomGenerator.getRandomNumberInRange(1, Building.numFloors);
        } while (currentFloor.getFloorIndex() == destinationFloorIndex);
    }

    public void setCurrentFloor(Floor currentFloor) {
        this.currentFloor = currentFloor;
    }

    public Direction getDirection() {
        return direction;
    }

    public void setDirection() {
        direction = Direction.getDirection(currentFloor.getFloorIndex(), destinationFloorIndex);
    }

    public void pushButton() {
        currentFloor.pushedButton(direction);
    }
}