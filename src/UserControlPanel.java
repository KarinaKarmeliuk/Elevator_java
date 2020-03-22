import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.InputMismatchException;
import java.util.Scanner;

public class UserControlPanel {

    public static void main(String[] args) throws IOException {

        DataOutputStream outputStream = null;
        DataInputStream inputStream = null;
        Socket clientSocket = null;

        try  {
            clientSocket = new Socket("localhost", 6545);
            while (true) { // general loop - for reset input/output streams if there was invalid input

                Scanner scanner = new Scanner(System.in);
                outputStream = new DataOutputStream(clientSocket.getOutputStream());
                inputStream = new DataInputStream(clientSocket.getInputStream());
                short userInput = 1;

                System.out.println("Tips:\n\tinput two numbers separated by spacer and press ENTER\n" +
                        "\t1st number - for passenger's current floor, 2nd - for passenger's destination floor\n" +
                        "\tinput \"0\" - for stop]");

                try {
                    while (true) { // for getting new messages from user to server
                        int loopCounter = 0;
                        String messageToServer = "";

                        while (loopCounter != 2) { // for getting 2 numbers from console
                            if (userInput == 0)
                                break;
                            userInput = scanner.nextShort();
                            messageToServer += userInput + " ";
                            loopCounter++;
                        }
                        if (userInput == 0) {
                            messageToServer = "0";
                            scanner.close();
                        }

                        messageToServer = messageToServer.trim();
                        outputStream.writeUTF(messageToServer);
                        outputStream.flush();

                        String echoFromServer = inputStream.readUTF();
                        if (echoFromServer.contentEquals("0")) {
                            System.out.println("Server echo - successfully disconnected. Get: " + echoFromServer);
                            break; // go out of loop of getting new messages
                        }
                        System.out.println(echoFromServer);
                    }
                } catch (InputMismatchException e) { // check where is continue
                    System.out.println("Invalid input! Start over your input.");
                    continue; // go to start of general loop again
                }
                break; // go out of loop of general loop
            }
        } catch (SocketException e) {
            if (clientSocket == null || !clientSocket.isConnected())
                System.out.println("Run server-app first!");
            else
                System.out.println("Server-app is disconnected");
        }
        finally {
            if (outputStream != null)
                outputStream.close();
            if (inputStream != null)
                inputStream.close();
        }
    }
}
