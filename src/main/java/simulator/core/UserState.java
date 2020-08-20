package simulator.core;

import com.domain.Packet;
import com.google.common.primitives.Bytes;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import simulator.domain.MessageType;
import simulator.load.LoadSimulator;
import simulator.properties.Constants;
import simulator.utility.UtilityClass;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.System.exit;

/**
 * This class manages state of the user
 * and it contains state managing
 * functions
 *
 * @author umar.tahir@afiniti.com
 *
 */

@Slf4j
@AllArgsConstructor
public class UserState {



    @Getter
    private final AtomicBoolean loggedInFlag = new AtomicBoolean(false);

    @Getter
    private final AtomicInteger magicNumberAssignedByServer = new AtomicInteger(0);

    @Getter
    private final AtomicInteger userIdOfClientAllocatedByServer = new AtomicInteger(0);

    private final Random randomGenerator = new Random();
    /**
     * This class initiates login process for the
     * user.
     */

    protected byte [] initiateLoginProcess(SocketChannel socketChannel) {
        log.info("Execution of initiateLoginProcess started");
        log.info("Calling loginLogoutMenu method");
        loginLogoutMenu();
        log.info("Calling loggingInTheChatApplication method");
        return loggingInTheChatApplication(socketChannel);
    }

    /**
     * This class initiates login and logout menu
     * and shows it to the user
     */

    private void loginLogoutMenu() {
        System.out.println("=========== Menu for Chat Client ===========");
        System.out.println("=========== Login:        Press \"1\" to login ===========");
        System.out.println("=========== Logout:       To logout anytime please type logout ===========");
        System.out.println("=========== Close:        To close the program anytime please Press \"0\" ===========");
        System.out.println("=========== To Send Message after LOGIN please follow this protocol: ID | Type your message ===========");
    }

    /**
     *
     * This method gets login packet and send it
     * to server
     *
     */

    private byte[] loggingInTheChatApplication(SocketChannel socketChannel) {
        log.info("Execution of loggingInTheChatApplication started");
        log.info("Calling chatMessageBuilder method");
        return getBytesArrayFromPacket(getLoginPacket(this));

    }

    /**
     * This method will keep thread busy until
     * user is logged in
     *
     * @apiNote after confirming you can use
     * wait() notify()
     */

    private void waitingForServerLoginResponse() {
        while (!getLoggedInFlag().get()) ;
    }

    /**
     * This method gets message to send to user
     *
     * @return packet
     *
     */

    public Packet getMessageToSend() {
        //critical section
        int randomElementIndex = randomGenerator.nextInt(LoadSimulator.getClientsConnectedToUser().size()) ;
        Integer destinationId = LoadSimulator.getClientsConnectedToUser().get(randomElementIndex);
        //critical section

        String message = destinationId+"| This is message from source with Id "+destinationId;
        return getMessagePacket(message, this);

    }

    /**
     * Method that converts byte array into packet class object
     *
     * @param message this is message in byte [] which needs to
     *                be converted
     * @return packet object
     */

    public Packet getPacketFromByteArray(byte[] message) {
        log.info("Execution of convertByteArrayIntoPacket method started");

        int magicBytes = UtilityClass.getIntFromByteArray(message, Constants.START_OF_MAGIC_BYTES_INCLUSIVE, Constants.END_OF_MAGIC_BYTES_EXCLUSIVE);

        String messageTypeValue = UtilityClass.getStringFromByteArray(message, Constants.START_OF_MESSAGE_TYPE_INCLUSIVE, Constants.END_OF_MESSAGE_TYPE_EXCLUSIVE);
        MessageType messageType = MessageType.fromTextGetMessageType(messageTypeValue).get();

        int sourceId = UtilityClass.getIntFromByteArray(message, Constants.START_OF_SOURCE_ID_INCLUSIVE, Constants.END_OF_SOURCE_ID_EXCLUSIVE);
        int destId = UtilityClass.getIntFromByteArray(message, Constants.START_OF_DEST_ID_INCLUSIVE, Constants.END_OF_DEST_ID_EXCLUSIVE);

        int messageLength = UtilityClass.getIntFromByteArray(message, Constants.START_OF_MESSAGE_LENGTH_INCLUSIVE, Constants.END_OF_MESSAGE_LENGTH_EXCLUSIVE);
        String messageOfClient = UtilityClass.getStringFromByteArray(message, Constants.START_OF_MESSAGE_INCLUSIVE, Constants.START_OF_MESSAGE_INCLUSIVE+messageLength);

        log.info("Execution of convertByteArrayIntoPacket method ended");

        return Packet.builder().magicBytes(magicBytes).messageType(messageType).messageSourceId(sourceId).messageDestinationId(destId)
                .messageLength(messageLength).message(messageOfClient).build();

    }

    /**
     * This class returns login packet that is built
     * on specific login inputs
     *
     * @return Login Packet
     */

    public Packet getLoginPacket(UserState userState) {
        log.info("Execution of loggingInTheChatApplication completed");
        log.info("Returning login packet message");

        return Packet.builder().magicBytes(userState.getMagicNumberAssignedByServer().get()).
                messageType(MessageType.LOGIN).messageSourceId(userState.getUserIdOfClientAllocatedByServer().get()).
                messageDestinationId(Constants.SERVER_SOURCE_ID).
                messageLength(Constants.NO_MASSAGE_LENGTH_DEFINED).
                message(Constants.LOGIN_MESSAGE).build();
    }

    /**
     * This method returns byte array from packet object
     *
     * @param packet input
     * @return byte []
     */

    public byte[] getBytesArrayFromPacket(Packet packet) {
        log.info("Calling convertMessagePacketIntoTheByteArray method");
        log.info("Execution of convertMessagePacketIntoTheByteArray method started");

        List<Byte> byteArrayList = new ArrayList<>();

        byteArrayList.addAll(Bytes.asList(ByteBuffer.allocate(4).putInt(packet.getMagicBytes()).array()));
        byteArrayList.addAll(Bytes.asList(packet.getMessageType().getMessageCode().getBytes()));

        byte bytes [] = new byte[2];
        bytes[0] = byteArrayList.get(4);
        bytes[1] = byteArrayList.get(5);

        if (new String(bytes).equals("T")){
            System.out.println("Umaaaaaaaaaaaaaaaar "+new String(bytes));
            exit(0);
        }


        byteArrayList.addAll(Bytes.asList(ByteBuffer.allocate(4).putInt(packet.getMessageSourceId()).array()));
        byteArrayList.addAll(Bytes.asList(ByteBuffer.allocate(4).putInt(packet.getMessageDestinationId()).array()));
        byteArrayList.addAll(Bytes.asList(ByteBuffer.allocate(4).putInt(packet.getMessageLength()).array()));
        byteArrayList.addAll(Bytes.asList(packet.getMessage().getBytes()));

        log.info("returning bytes array");
        log.info("Execution of convertMessagePacketIntoTheByteArray method ended");

        return Bytes.toArray(byteArrayList);

    }

    /**
     * This method returns message packet which we need to send to user
     *
     * @param message input message
     * @return packet
     */

    public Packet getMessagePacket(String message, UserState userState) {
        log.info("Execution of getMessagePacket started");
        String[] parts = message.split("\\|");

        return Packet.builder().magicBytes(userState.getMagicNumberAssignedByServer().get()).messageType(MessageType.DATA).
                messageSourceId(userState.getUserIdOfClientAllocatedByServer().get()).messageDestinationId(Integer.parseInt(parts[0].trim())).
                messageLength(parts[1].length()).message(parts[1]).build();

    }

    /**
     * This method returns logout packet to send to server
     *
     * @return packet
     */

    public Packet getLogOutPacket(UserState userState) {
        return Packet.builder().magicBytes(userState.getMagicNumberAssignedByServer().get()).messageType(MessageType.LOGOUT).
                messageSourceId(userState.getUserIdOfClientAllocatedByServer().get()).messageDestinationId(Constants.SERVER_SOURCE_ID).
                messageLength(Constants.NO_MASSAGE_LENGTH_DEFINED).message(Constants.LOGOUT_MESSAGE).build();

    }


}
