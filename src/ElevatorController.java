import java.util.LinkedHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

public class ElevatorController {

    private Elevator elevator;
    //private Map<Integer, Floor> callingFloorsTable;
    private LinkedHashMap<Integer, Floor> callingFloorsTable;
    private ExecutorService executorService;
    private FutureTask<Void> futureTask;

    public ElevatorController(Elevator elevator) {
        this.elevator = elevator;
        //callingFloorsTable = Collections.synchronizedMap(new LinkedHashMap<>(Building.numFloors + 10));
        callingFloorsTable = new LinkedHashMap<>(Building.numFloors + 10);
        executorService = Executors.newCachedThreadPool();
    }

    public void addCallToCFTable(Floor callingFloor) {
        callingFloorsTable.put(callingFloor.getFloorIndex(), callingFloor);
    }

    public void initiateElevatorWork(Floor callingFloor) {
        if (elevator.getState() == Elevator.State.IDLE) {
            elevator.setDirection(callingFloor.determineDirection());
            if (elevator.getDirection() == Direction.UP)
                elevator.setDestinationFloor(callingFloor.findMaxFloorDestination());
            else
                elevator.setDestinationFloor(callingFloor.findMinFloorDestination());
            elevator.setCallingFloorsTable(callingFloorsTable);
            futureTask = new FutureTask<>(elevator, null);
            executorService.submit(futureTask);

            while (true) {
                if (futureTask.isDone()) {
                    break;
                }
                if (elevator.step.get() >= 50) {
                    executorService.shutdown();
                    return;
                }
            }
            // begin recursion of this method till the elevator makes 50 or little more steps
            if (callingFloorsTable.containsKey(elevator.getCurrentFloor()))
                initiateElevatorWork(callingFloorsTable.get(elevator.getCurrentFloor()));
            else {
                int key;
                for (key = 1; key <= Building.numFloors; key++) {
                    if (callingFloorsTable.containsKey(key))
                        break;
                }
                initiateElevatorWork(callingFloorsTable.get(key));
            }
        }
    }
}
