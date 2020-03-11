import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class Elevator implements Runnable {

    public enum State {
        IDLE,
        MOVING,
        STOPPED
    }

    public AtomicInteger step;
    private State state;
    private final int PASSENGERS_LIMIT;
    private Floor floors[];
    private int currentFloor;
    private int destinationFloor;
    private Direction direction;
    private ArrayList<Passenger> travellingPassengers; // can use ArrayList, because it used only by Elevator-thread
    private Map<Integer, Floor> callingFloorsTable;

    public void setCallingFloorsTable(Map<Integer, Floor> callingFloorsTable) {
        this.callingFloorsTable = callingFloorsTable;
    }

    public Elevator() {
        step = new AtomicInteger(0);
        state = State.IDLE;
        PASSENGERS_LIMIT = 5;
        currentFloor = 1;
        destinationFloor = 1;
        direction = Direction.NONE;
        travellingPassengers = new ArrayList<>(5);
    }

    @Override
    public void run() {
        callProcessing();
    }

    private void pickPassengers(LinkedList<Passenger> waitingPassengers) {

        if (waitingPassengers.isEmpty())
            return;

        Iterator<Passenger> iterator = waitingPassengers.iterator();
        Passenger passenger;
        while (iterator.hasNext()) {
            passenger = iterator.next();

            if (direction == passenger.getDirection()) {

                switch (direction) {
                    case UP:
                        if (passenger.getDestinationFloorIndex() > destinationFloor)
                            destinationFloor = passenger.getDestinationFloorIndex();
                        break;
                    case DOWN:
                        if (passenger.getDestinationFloorIndex() < destinationFloor)
                            destinationFloor = passenger.getDestinationFloorIndex();
                }

                passenger.getCurrentFloor().releasedButton(direction);
                travellingPassengers.add(passenger);

                iterator.remove();

                if (travellingPassengers.size() == PASSENGERS_LIMIT)
                    break;
            }
        }
    }

    private void passengerExit(Passenger passenger) {
        passenger.setCurrentFloor(floors[currentFloor - 1]);
        passenger.setDestinationFloorIndex();
        passenger.setDirection();
        floors[currentFloor - 1].addWaitingPassenger(passenger);
        passenger.pushButton();
    }

    private void checkPassengersLeaving() {
        Iterator<Passenger> iterator = travellingPassengers.iterator();
        Passenger passenger;
        while (iterator.hasNext()) {
            passenger = iterator.next();
            if (currentFloor == passenger.getDestinationFloorIndex()) {
                if (state != State.STOPPED)
                    state = State.STOPPED;
                iterator.remove();
                passengerExit(passenger);
            }
        }
    }

    private void dropOffPassengers() {
        for (Passenger passenger : travellingPassengers) {
            passenger.setCurrentFloor(floors[currentFloor - 1]);
            passengerExit(passenger);
        }
        travellingPassengers.clear();
    }

    private void checkCalls() {
        if (callingFloorsTable.containsKey(currentFloor)) { // check if there is any UP/DOWN call for current floor

            Floor callingFloor = callingFloorsTable.get(currentFloor);
            switch (direction) { // check if there are co-directional UP/DOWN calls for elevator moving
                case UP:
                    if (callingFloor.getButtonUp() > 0) {
                        state = State.STOPPED;
                        pickPassengers(callingFloor.getWaitingPassengers());
                    }
                    break;
                case DOWN:
                    if (callingFloor.getButtonDown() > 0) {
                        state = State.STOPPED;
                        pickPassengers(callingFloor.getWaitingPassengers());
                    }
            }
        }
    }

    private void callProcessing() {
        state = State.MOVING;
        do {
            step.incrementAndGet();
            printStep();

            if (!travellingPassengers.isEmpty()) { // let passengers leave the elevator if they reach destination floor
                checkPassengersLeaving();
                if (travellingPassengers.size() < PASSENGERS_LIMIT)  // pick passengers if there is free space
                    checkCalls();
            } else
                checkCalls();

            if (state != State.MOVING)
                state = State.MOVING;

            nextFloor();

        } while (currentFloor != destinationFloor);

        state = State.STOPPED;
        step.incrementAndGet();
        dropOffPassengers();
        destinationFloor = currentFloor;
        state = State.IDLE;
        direction = Direction.NONE;
        printStep();
    }

    private void nextFloor() {
        if (state == State.MOVING) {
            if (direction == Direction.UP)
                currentFloor++;
            else if (this.direction == Direction.DOWN)
                currentFloor--;
        }
    }

    private void printStep() {

        System.out.println("\n*** Step " + step + " ***");
        for (int i = floors.length - 1; i >= 0; i--) {
            System.out.print("Floor_" + floors[i].getFloorIndex() + "|");
            if (currentFloor == floors[i].getFloorIndex()) {
                System.out.printf("%-4s|travelling passengers: ", direction.name());
                if (!travellingPassengers.isEmpty())
                    travellingPassengers.forEach(passenger ->
                            System.out.printf("%d ", passenger.getDestinationFloorIndex()));
            }
            System.out.print("\t|waiting passengers: ");
            if (!floors[i].getWaitingPassengers().isEmpty()) {
                floors[i].getWaitingPassengers().forEach(passenger ->
                        System.out.printf("%d ", passenger.getDestinationFloorIndex()));
            }
            System.out.println();
        }
    }

    public State getState() {
        return state;
    }

    public int getCurrentFloor() {
        return currentFloor;
    }

    public void setDestinationFloor(int destinationFloor) {
        this.destinationFloor = destinationFloor;
    }

    public void setFloors(Floor[] floors) {
        this.floors = floors;
    }

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }
}
