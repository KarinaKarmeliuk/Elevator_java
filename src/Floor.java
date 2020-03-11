import java.util.LinkedList;

public class Floor {

    private final int floorIndex;
    private ElevatorController elevatorController;
    private int buttonUp;
    private int buttonDown;
    private LinkedList<Passenger> waitingPassengers;

    public Floor(int floorIndex, ElevatorController elevatorController) {
        this.floorIndex = floorIndex;
        int numPassengers = RandomGenerator.getRandomNumberInRange(0, 10);
        waitingPassengers = new LinkedList<>();

        if (numPassengers != 0) {
            for (int i=0; i < numPassengers; i++) {
                waitingPassengers.add(new Passenger(this));
            }
            for (Passenger passenger : waitingPassengers) {
                passenger.pushButton();
            }
            elevatorController.addCallToCFTable(this);
        }
    }

    public void pushedButton(Direction direction) {
        if (direction == Direction.UP)
            buttonUp++;
        else if (direction == Direction.DOWN)
            buttonDown++;
    }

    public void releasedButton(Direction direction) {
        if (direction == Direction.UP)
            buttonUp--;
        else if (direction == Direction.DOWN)
            buttonDown--;
    }

    public int findMaxFloorDestination() {
        int max = waitingPassengers.getFirst().getDestinationFloorIndex();
        for (Passenger passenger : waitingPassengers) {
            if (passenger.getDestinationFloorIndex() > max)
                max = passenger.getDestinationFloorIndex();
        }
        return max;
    }

    public int findMinFloorDestination() {
        int min = waitingPassengers.getFirst().getDestinationFloorIndex();
        for (Passenger passenger : waitingPassengers) {
            if (passenger.getDestinationFloorIndex() < min)
                min = passenger.getDestinationFloorIndex();
        }
        return min;
    }

    public Direction determineDirection() {
        if (buttonUp >= buttonDown)
            return Direction.UP;
        else
            return Direction.DOWN;
    }

    public int getFloorIndex() {
        return floorIndex;
    }

    public int getButtonUp() {
        return buttonUp;
    }

    public int getButtonDown() {
        return buttonDown;
    }

    public LinkedList<Passenger> getWaitingPassengers() {
        return waitingPassengers;
    }

    public void addWaitingPassenger(Passenger passenger) {
        waitingPassengers.addLast(passenger);
    }

    public int getNumWaitingPassenger() {
        return waitingPassengers.size();
    }
}
