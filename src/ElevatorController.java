import java.util.LinkedHashMap;
import java.util.Map;

public class ElevatorController {

    private Elevator elevator;
    private Map<Integer, Floor> callingFloorsTable;

    public ElevatorController(Elevator elevator) {
        this.elevator = elevator;
        callingFloorsTable = new LinkedHashMap<>(Building.numFloors + 10);
    }

    public void addCallToCFTable(Floor callingFloor) {
        callingFloorsTable.put(callingFloor.getFloorIndex(), callingFloor);
    }

    public void initiateElevatorWork() {

        int totalWaitingPassengers = 0;

        for (Floor floor : callingFloorsTable.values()) {
            totalWaitingPassengers += floor.getWaitingPassengers().size();
        }
        elevator.setTotalWaitingPassengers(totalWaitingPassengers);

        if (elevator.getState() == Elevator.State.IDLE) {
            elevator.setCallingFloorsTable(callingFloorsTable);
            elevator.callProcessing();
        }
    }
}
