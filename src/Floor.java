import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class Floor {

    private final int floorIndex;
    private int outPassengers;
    private AtomicInteger buttonUp;
    private AtomicInteger buttonDown;
    private List<Passenger> waitingPassengers;

    public Floor(int floorIndex, ElevatorController elevatorController) {
        this.floorIndex = floorIndex;
        outPassengers = 0;
        buttonUp = new AtomicInteger(0);
        buttonDown = new AtomicInteger(0);
        int numPassengers = RandomGenerator.getRandomNumberInRange(0, 10);
        waitingPassengers = Collections.synchronizedList(new LinkedList<>());

        if (numPassengers != 0) {
            for (int i=0; i < numPassengers; i++) {
                waitingPassengers.add(new Passenger(this));
            }
            elevatorController.addCallToCFTable(this);
        }
    }

    synchronized public void addPassenger(int destinationFloor) {
        waitingPassengers.add(new Passenger(this, destinationFloor));
    }

    public void pushedButton(Direction direction) {
        if (direction == Direction.UP)
            buttonUp.incrementAndGet();
        else if (direction == Direction.DOWN)
            buttonDown.incrementAndGet();
    }

    public void releasedButton(Direction direction) {
        if (direction == Direction.UP)
            buttonUp.decrementAndGet();
        else if (direction == Direction.DOWN)
            buttonDown.decrementAndGet();
    }

    public Direction determineDirection() {
        if (buttonUp.get() >= buttonDown.get())
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
        return buttonUp.get();
    }

    public int getButtonDown() {
        return buttonDown.get();
    }

    public List<Passenger> getWaitingPassengers() {
        return waitingPassengers;
    }
}
