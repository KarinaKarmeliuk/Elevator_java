import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicInteger;

public class ElevatorController {

    private Elevator elevator;
    private Floor floors[];
    private ConcurrentNavigableMap<Integer, Floor> callingFloorsTable;
    private AtomicInteger totalWaitingPassengers;
    private Thread elevatorThread;
    private CallHandler callHandler;
    private ServerSocket serverSocket;
    private int port;

    public ElevatorController(Elevator elevator) {
        this.elevator = elevator;
        callingFloorsTable = new ConcurrentSkipListMap<>();
        totalWaitingPassengers = new AtomicInteger(0);
        elevatorThread = new Thread(elevator);
        port = 6545;
    }

    public void addCallToCFTable(Floor callingFloor) {
        callingFloorsTable.put(callingFloor.getFloorIndex(), callingFloor);
    }

    public boolean isElevatorThreadAlive() {
        return elevatorThread.isAlive();
    }

    public void initiateElevatorWork() {

        // get the total number of waiting passengers on all floors
        for (Floor floor : callingFloorsTable.values()) {
            totalWaitingPassengers.addAndGet(floor.getWaitingPassengers().size());
        }

        try {
            // run handler to accept calls from User-Control-Panel
            serverSocket = new ServerSocket(port);
            serverSocket.setSoTimeout(2000);
            callHandler = new CallHandler(serverSocket, this);
            callHandler.start();

            // run process of User-Control-Panel
            // ...will be added soon...

            if (elevator.getState() == Elevator.State.IDLE)
                elevatorThread.start();

            elevatorThread.join();
            callHandler.join(3000);
            // stop handler if elevatorThread is finished, but User-Control-Panel is still connected
            if (!serverSocket.isClosed())
                callHandler.stopCallHandler();
            callHandler.join();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // accept calls from User-Control-Panel
    public void acceptCall(int callingFloorIndex, int destinationFloor) {
        totalWaitingPassengers.incrementAndGet();
        elevator.passengerAddedByUser.compareAndSet(false, true);
        floors[callingFloorIndex-1].addPassenger(destinationFloor);
        if (!callingFloorsTable.containsKey(callingFloorIndex))
            addCallToCFTable(floors[callingFloorIndex-1]);
    }

    public void setFloors(Floor[] floors) {
        this.floors = floors;
    }

    public ConcurrentNavigableMap<Integer, Floor> getCallingFloorsTable() {
        return callingFloorsTable;
    }

    public AtomicInteger getTotalWaitingPassengers() {
        return totalWaitingPassengers;
    }
}
