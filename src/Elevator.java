import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class Elevator implements Runnable {

    public enum State {
        IDLE,
        MOVING,
        STOPPED
    }

    private final int PASSENGERS_LIMIT;
    private int step;
    private State state;
    private Floor floors[];
    private int currentFloor;
    private int destinationFloor;
    private Direction direction;
    private List<Passenger> travellingPassengers;
    private int totalOutPassengers;
    private ElevatorController elevatorController;
    AtomicBoolean passengerAddedByUser;

    public Elevator() {
        PASSENGERS_LIMIT = 5;
        step = 0;
        state = State.IDLE;
        currentFloor = 1;
        destinationFloor = 1;
        direction = Direction.NONE;
        travellingPassengers = new ArrayList<>(5);
        totalOutPassengers = 0;
        passengerAddedByUser = new AtomicBoolean(false);
    }

    @Override
    public void run() {
        callProcessing();
    }

    private void pickPassengers(List<Passenger> waitingPassengers) {

        if (waitingPassengers.isEmpty())
            return;

        Iterator<Passenger> iterator = waitingPassengers.iterator();
        Passenger passenger;
        while (iterator.hasNext()) {
            passenger = iterator.next();

            if (direction == passenger.getDirection()) {

                switch (direction) {
                    case UP:
                        if (passenger.getDestinationFloor() > destinationFloor)
                            destinationFloor = passenger.getDestinationFloor();
                        break;
                    case DOWN:
                        if (passenger.getDestinationFloor() < destinationFloor)
                            destinationFloor = passenger.getDestinationFloor();
                }

                passenger.getCurrentFloor().releasedButton(direction);
                travellingPassengers.add(passenger);

                iterator.remove();
                elevatorController.getTotalWaitingPassengers().decrementAndGet();

                if (travellingPassengers.size() == PASSENGERS_LIMIT)
                    break;
            }
        }
        if (waitingPassengers.isEmpty())
            elevatorController.getCallingFloorsTable().remove(currentFloor);
    }

    private void checkPassengersLeaving() {
        Iterator<Passenger> iterator = travellingPassengers.iterator();
        Passenger passenger;
        while (iterator.hasNext()) {
            passenger = iterator.next();
            if (currentFloor == passenger.getDestinationFloor()) {
                if (state != State.STOPPED)
                    state = State.STOPPED;
                iterator.remove();
                floors[currentFloor-1].incrementOutPassengers();
                totalOutPassengers++;
            }
        }
    }

    private void checkCalls() {
        // check if there is any UP/DOWN call for current floor
        if (elevatorController.getCallingFloorsTable().containsKey(currentFloor)) {
            if (travellingPassengers.isEmpty())
                direction = Direction.NONE;

            Floor callingFloor = elevatorController.getCallingFloorsTable().get(currentFloor);
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
                    break;
                case NONE:
                    direction = callingFloor.determineDirection();
                    state = State.STOPPED;
                    pickPassengers(callingFloor.getWaitingPassengers());
            }
        }
    }

    private void resetElevator() {
        state = State.STOPPED;
        checkPassengersLeaving();
        state = State.IDLE;
        direction = Direction.NONE;
    }

    public void callProcessing() {
        state = State.MOVING;

        while (elevatorController.getTotalWaitingPassengers().get() > 0) {
            // when the elevator's state is IDLE, but there are still waiting passengers in building:
            // the elevator's priority for going down the lowest or going up the highest calling floor
            // depends on its current floor
            if ( (Building.numFloors  - currentFloor) < (Building.numFloors/2)) // go down
                destinationFloor = elevatorController.getCallingFloorsTable().lastEntry().getValue().getFloorIndex();
            else // go up
                destinationFloor = elevatorController.getCallingFloorsTable().firstEntry().getValue().getFloorIndex();
            direction = Direction.getDirection(currentFloor, destinationFloor);

            do {
                step++;

                if (!travellingPassengers.isEmpty()) {
                    checkPassengersLeaving(); // let passengers leave the elevator if they reach destination floor
                    if (travellingPassengers.size() < PASSENGERS_LIMIT)  // pick passengers if there is free space
                        checkCalls();
                } else
                    checkCalls();

                if (state != State.MOVING)
                    state = State.MOVING;

                printStep();
                nextFloor();

            } while (currentFloor != destinationFloor);

            resetElevator();
        }

        step++;
        resetElevator();
        printStep();
    }

    private void nextFloor() {
        if (state == State.MOVING) {
            if (direction == Direction.UP)
                currentFloor++;
            else if (this.direction == Direction.DOWN)
                currentFloor--;
        }
        // sleep() is called for the user to be managed to input data from User-Control-Panel
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void printStep() {

        System.out.println("\n*** Step " + step + " ***");
        char spacer = '|';

        for (int i = floors.length - 1; i >= 0; i--) {
            System.out.printf("%-2d %2d.%c",
                    floors[i].getOutPassengers(),
                    floors[i].getFloorIndex(),
                    spacer
            );
            if (currentFloor == floors[i].getFloorIndex()) {
                char directionSign = ' ';
                switch (direction) {
                    case UP:
                        directionSign = '^';
                        break;
                    case DOWN:
                        directionSign = 'v';
                        break;
                    case NONE:
                        directionSign = '-';
                }
                System.out.print(directionSign);
                Iterator<Passenger> passengerIterator = travellingPassengers.iterator();
                for (int j = 0; j < PASSENGERS_LIMIT; j++) {
                    if (passengerIterator.hasNext())
                        System.out.printf("%3d", passengerIterator.next().getDestinationFloor());
                    else
                        System.out.printf("%3c", '.');
                }
                System.out.printf("%3c", directionSign);
                System.out.printf("%c", spacer);
            } else
                System.out.printf("%20c", spacer);
            if (!floors[i].getWaitingPassengers().isEmpty()) {
                floors[i].getWaitingPassengers().forEach(passenger ->
                        System.out.printf("%d ", passenger.getDestinationFloor()));
            }
            System.out.println();
        }
        System.out.printf("Total out: %d  Total waiting: %d\n", totalOutPassengers, elevatorController.getTotalWaitingPassengers().intValue());
        if (passengerAddedByUser.getAndSet(false)) {
            System.out.println("[Waiting passenger is added by user!]");
        }
    }

    public void setElevatorController(ElevatorController elevatorController) {
        this.elevatorController = elevatorController;
    }

    public State getState() {
        return state;
    }

    public void setFloors(Floor[] floors) {
        this.floors = floors;
    }
}
