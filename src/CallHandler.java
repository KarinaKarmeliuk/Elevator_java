import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CallHandler extends Thread {

    private final ExecutorService executorService;
    private ElevatorController elevatorController;
    private ServerSocket serverSocket;
    private Socket clientSocket;
    private DataOutputStream outputStream;
    private DataInputStream inputStream;

    public CallHandler(ServerSocket serverSocket, ElevatorController elevatorController) {
        this.serverSocket = serverSocket;
        this.elevatorController = elevatorController;
        executorService = Executors.newFixedThreadPool(5);
        clientSocket = null;
        outputStream = null;
        inputStream = null;
    }

    public void stopCallHandler() {
        try {
            clientSocket.close();
        } catch (IOException e) {
        }
    }

    private void disconnectClient() throws IOException, InterruptedException {
        outputStream.writeUTF("0");
        Thread.sleep(500);
        clientSocket.close();
    }

    @Override
    public void run() {

        try {
            while (true) {

                try {
                    clientSocket = serverSocket.accept();
                } catch (SocketTimeoutException e) {// if the timeout for waiting connection is reached
                    if (elevatorController.isElevatorThreadAlive())
                        continue;
                    else
                        break;
                }

                outputStream = new DataOutputStream(clientSocket.getOutputStream());
                inputStream = new DataInputStream(clientSocket.getInputStream());

                while (!clientSocket.isOutputShutdown()) { // while client is available

                    String inFromClient = inputStream.readUTF();
                    int userInput[];

                    if (inFromClient.contentEquals("0")) {
                        disconnectClient();
                        break;
                    } else {
                        if (elevatorController.isElevatorThreadAlive()) {
                            if (inFromClient.isEmpty())
                                outputStream.writeUTF("Server echo - LOST MESSAGE:(");
                            else {
                                if (inFromClient.contains("-"))
                                    inFromClient = inFromClient.replaceAll("-", "");

                                String splittedMessages[] = inFromClient.split(" ");
                                userInput = new int[2];
                                for (int i = 0; i < userInput.length; i++) {
                                    userInput[i] = Integer.parseInt(splittedMessages[i]);
                                }

                                // check if user data is valid
                                if (    userInput[0] > Building.numFloors ||
                                        userInput[1] > Building.numFloors ||
                                        userInput[0] == userInput[1]
                                        )
                                    outputStream.writeUTF("Server echo - Invalid data!");
                                else {
                                    outputStream.writeUTF("Server echo - OK:) Get: " + inFromClient);
                                    Runnable handlerTask = () -> elevatorController.acceptCall(userInput[0], userInput[1]);
                                    executorService.submit(handlerTask);
                                }
                            }
                        } else { // disconnection if elevatorThread is not alive
                            disconnectClient();
                            break;
                        }
                    }
                    outputStream.flush();
                }
                if (!elevatorController.isElevatorThreadAlive())
                    break;
            }
        } catch (SocketException e) {
            System.out.println("\nCallHandler is closed while User-Control-Panel was connected. Input calls are no more accepted");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            if (!elevatorController.isElevatorThreadAlive()) {
                try {
                    executorService.shutdown();
                    if (clientSocket != null)
                        if (!clientSocket.isClosed())
                            clientSocket.close();
                    if (inputStream != null)
                        inputStream.close();
                    if (outputStream != null)
                        outputStream.close();
                    if (!serverSocket.isClosed())
                        serverSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
