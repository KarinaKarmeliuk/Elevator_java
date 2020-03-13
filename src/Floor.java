import java.util.LinkedList;

public class Floor {

    private final int floorIndex;
    private int outPassengers;
    private int buttonUp;
    private int buttonDown;

    private LinkedList<Passenger> waitingPassengers;

    public Floor(int floorIndex, ElevatorController elevatorController) {
        this.floorIndex = floorIndex;
        outPassengers = 0;
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

    public Direction determineDirection() {
        if (buttonUp >= buttonDown)
            return Direction.UP;
        else
            return Direction.DOWN;
    }

    public int getFloorIndex() {
        return floorIndex;
    }

    public int getOutPassengers() {
        return outPassengers;
    }

    public void incrementOutPassengers() {
        outPassengers++;
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
}
