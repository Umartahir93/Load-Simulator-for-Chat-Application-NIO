package simulator.core;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.domain.Packet;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;


/**
 * This class writes message to the client.
 * It will keep accepting the messages from
 * user to sent to other user until socket
 * remains open
 *
 * @author umar.tahir@afiniti.com
 */


@Slf4j
@RequiredArgsConstructor
public class Writer extends Thread {
    private final SocketChannel clientSocketChannel;
    private final UserState userState;


    /**
     * Whenever chat thread starts, it will
     * start writing messages to server
     * which will in turn be forwarded to
     * specific client
     */

    @Override
    public void run() {
        log.info("Execution of writer thread started");

        try {

            log.info("Calling initiateRequestToServerFlow() method");
            initiateRequestToServerFlow();

        } catch (Exception exception) {
            log.error("Error occurred while trying to send message to server ");
            log.error("Exception Occurred ", exception);
            log.error("Closing writer thread");
            exception.printStackTrace();
        }
    }

    /**
     * This method starts initiating request flow to server
     * Here we check if you are login. If you are than send
     * message. Otherwise initiate login process.
     */
    public void initiateRequestToServerFlow() throws InterruptedException {
        log.info("Execution of initiateRequestToServerFlow method started");

        while (clientSocketChannel.isOpen()) {
            log.info("Client socket channel is open checking user id of client assigned by server");
            if(userState.getUserIdOfClientAllocatedByServer().get() != 0){
                if (userState.getLoggedInFlag().get()) {
                    log.info("User is logged in");
                    log.info("Calling startSendingMessagesToServer() method");
                    startSendingMessagesToServer();
                } else {
                    log.info("User is not logged in");
                    log.info("Calling initiateLoginProcess() method");
                    byte [] message = userState.initiateLoginProcess(clientSocketChannel);
                    writingMessageToServer(message);
                    waitingForLoginResponseFromServer();

                }
            }
        }
        log.info("Execution of initiateRequestToServerFlow method ended");
    }

    private void waitingForLoginResponseFromServer() throws InterruptedException {
        log.info("Execution of waitingForLoginResponseFromServer started");

        synchronized (userState.getLoggedInFlag()){
            while(!userState.getLoggedInFlag().get()){
                userState.getLoggedInFlag().wait();
            }
        }

        log.info("Execution of waitingForLoginResponseFromServer ended");
    }

    /**
     * This method starts sending message to server.
     * It will take input and converts it into byte []
     * array and than send message to server
     */

    private void startSendingMessagesToServer() {
        synchronized (Writer.class) {
            log.info("Execution of startSendingMessagesToServer method started");
            log.info("Calling takeAndAnalyzeUserInput method");
            Packet packet = userState.getMessageToSend();

            log.info("Calling writingMessageToServer method");
            writingMessageToServer(userState.getBytesArrayFromPacket(packet));
            log.info("Execution of startSendingMessagesToServer method started");
        }
    }

    public int writingMessageToServer(byte[] messagePacketInBytes) {
        log.info("Execution of writingMessageToServer started");
        int bytes = 0;

        log.info("Creating buffer with allocation of private backend space with size {}", messagePacketInBytes.length);
        ByteBuffer messageToServerBuffer = ByteBuffer.allocate(messagePacketInBytes.length);
        messageToServerBuffer.put(messagePacketInBytes);
        messageToServerBuffer.flip();

        log.info("Sending message to the server");
        try {
            bytes = clientSocketChannel.write(messageToServerBuffer);

        } catch (IOException e) {
            log.error("Error sending message");
            e.printStackTrace();
        }

        log.info("Message sent to server");
        log.info("Clearing the buffer");
        messageToServerBuffer.clear();

        log.info("Execution of sendMessageToServer ended");
        return bytes;
    }
}
