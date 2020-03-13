import java.util.*;

public class Elevator {

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
    private Map<Integer, Floor> callingFloorsTable;
    private int totalWaitingPassengers;
    private int totalOutPassengers;

    public void setCallingFloorsTable(Map<Integer, Floor> callingFloorsTable) {
        this.callingFloorsTable = callingFloorsTable;
    }

    public Elevator() {
        PASSENGERS_LIMIT = 5;
        step = 0;
        state = State.IDLE;
        currentFloor = 1;
        destinationFloor = 1;
        direction = Direction.NONE;
        travellingPassengers = new ArrayList<>(5);
        totalWaitingPassengers = 0;
        totalOutPassengers = 0;
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
                totalWaitingPassengers--;

                if (travellingPassengers.size() == PASSENGERS_LIMIT)
                    break;
            }
        }
        if (waitingPassengers.isEmpty())
            callingFloorsTable.remove(currentFloor);
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
                floors[currentFloor-1].incrementOutPassengers();
                totalOutPassengers++;
            }
        }
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
                    break;
                case NONE:
                    direction = callingFloor.determineDirection();
                    state = State.STOPPED;
                    pickPassengers(callingFloor.getWaitingPassengers());
            }
        }
    }

    public void callProcessing() {
        state = State.MOVING;

        while (totalWaitingPassengers > 0) {
            Set<Integer> entrySet = callingFloorsTable.keySet();
            Iterator<Integer> iterator = entrySet.iterator();
            if (iterator.hasNext())
                destinationFloor = iterator.next();
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
        }

        state = State.STOPPED;
        step++;
        checkPassengersLeaving();
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
            System.out.printf("%-2d %2d.|",
                    floors[i].getOutPassengers(),
                    floors[i].getFloorIndex()
            );
            if (currentFloor == floors[i].getFloorIndex()) {
                String directionSign = "";
                switch (direction) {
                    case UP:
                        directionSign = "^";
                        break;
                    case DOWN:
                        directionSign = "v";
                        break;
                    case NONE:
                        directionSign = "-";
                }
                System.out.print(directionSign);
                Iterator<Passenger> passengerIterator = travellingPassengers.iterator();
                for (int j = 0; j < PASSENGERS_LIMIT; j++) {
                    if (passengerIterator.hasNext())
                        System.out.printf(" %d", passengerIterator.next().getDestinationFloorIndex());
                    else
                        System.out.print(" .");
                }
                System.out.print(" " + directionSign);
            } else
                System.out.print(" ");
            System.out.print("|");
            if (!floors[i].getWaitingPassengers().isEmpty()) {
                floors[i].getWaitingPassengers().forEach(passenger ->
                        System.out.printf("%d ", passenger.getDestinationFloorIndex()));
            }
            System.out.println();
        }
        System.out.printf("Total out: %d  Total waiting: %d\n", totalOutPassengers, totalWaitingPassengers);
    }

    public State getState() {
        return state;
    }

    public void setFloors(Floor[] floors) {
        this.floors = floors;
    }

    public void setTotalWaitingPassengers(int totalWaitingPassengers) {
        this.totalWaitingPassengers = totalWaitingPassengers;
    }
}
