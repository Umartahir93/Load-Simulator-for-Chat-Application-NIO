package simulator.core;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import simulator.load.LoadSimulator;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.time.Duration;
import java.time.Instant;

@Slf4j
@RequiredArgsConstructor
public class InternalCore {
    private final int serverSocketChannelPort;
    private final String hostName;
    private SocketChannel socketChannel;
    private final UserState userState = new UserState();
    private final Reader reader = new Reader();


    /**
     * This method is the starting point of our client
     * Here we are setting up the communication and
     * monitoring reading events and starting a new
     * writer thread
     */

    public void initiateApplication() {
        log.info("initiateApplication() method execution started");
        Selector selector = null;
        try {

            log.info("Calling setUpChannelAndSelector method()");
            selector = setUpChannelAndSelector();

            log.info("Calling createMessageWriterThread method()");
            createMessageWriterThread(socketChannel);

            log.info("Calling processSelectorReadEvent method()");
            processSelectorReadEvent(selector);


        } catch (Exception exception) {
            log.error("Cause of Error is ", exception);

        } finally {
            log.info("Executing finally block");
            finallyBlockExecutionForGraceFulShutdown(selector);
            log.info("initiateApplication method execution ended");
        }

    }

    /**
     * This method is used to set up the channel and selector.
     * Here we register set up channel with selector on read
     * events
     *
     * @return selector
     * @throws IOException is dealt above
     */
    private Selector setUpChannelAndSelector() throws IOException {
        log.info("setUpChannelAndSelector method() execution started");

        InetSocketAddress hostAddress = getSocketAndProduceAcceptanceDuration();
        socketChannel = SocketChannel.open(hostAddress);
        socketChannel.configureBlocking(false);
        log.info("Socket channel opened that represents connection with server at port {}", serverSocketChannelPort);

        log.info("Opening Selector");
        Selector selector = Selector.open();
        log.info("Selector opened");

        log.info("Registering socket with selector");
        socketChannel.register(selector, SelectionKey.OP_READ);

        log.info("Execution of setUpChannelAndSelector ended");

        return selector;
    }


    /**
     * This method acts as producer for connection acceptance time
     * As soon as connection with server is established we record
     * that time and put in the map
     *
     * @return socket address
     *
     */

    private InetSocketAddress getSocketAndProduceAcceptanceDuration() {
        log.info("Opening a Socket channel that represents connection with server at port {}", serverSocketChannelPort);
        Instant starts = Instant.now();
        InetSocketAddress hostAddress = new InetSocketAddress(hostName, serverSocketChannelPort);
        Instant ends = Instant.now();
        //critical section starts
        LoadSimulator.getConnectionAcceptanceTime().put(Thread.currentThread().getName(), Duration.between(starts,ends).toMillis());
        //critical section ends
        return hostAddress;
    }

    /**
     * This method will create writer thread and start
     * the writer thread
     *
     * @param socketChannel
     */

    private void createMessageWriterThread(SocketChannel socketChannel) {
        log.info("createMessageWriterThread method execution started");
        Thread messageWriterThread = new Writer(socketChannel, userState);
        log.info("Going to create writer thread");
        messageWriterThread.setDaemon(true);
        messageWriterThread.start();
    }


    /**
     * This method monitors read events and start processing
     * read events
     *
     * @param selector with registered channel
     * @throws IOException we deal this exception above
     */

    private void processSelectorReadEvent(Selector selector) throws IOException {
        log.info("Started channelEventsListenerViaSelector execution method");

        while (socketChannel.isOpen()) {
            log.info("Waiting for event to occur");
            selector.select();

            for (SelectionKey selectionKey : selector.selectedKeys()) {
                log.info("Retrieving key's ready-operation set");
                selectionKey.readyOps();

                log.info("Removing the selection key");
                selector.selectedKeys().remove(selectionKey);

                if (selectionKey.isValid() && selectionKey.isReadable()) {
                    log.info("Read event has occurred");
                    log.info("Calling reading from server method");
                    reader.readingFromServer((SocketChannel) selectionKey.channel(), userState);
                }

            }
        }

    }

    /**
     * Graceful shutdown of client
     *
     * @param selector takes input
     */
    private void finallyBlockExecutionForGraceFulShutdown(Selector selector) {
        log.info("Execution of selectorShutdown started");
        if (selector != null && selector.isOpen()) {

            for (SelectionKey selectionKey : selector.keys()) {
                log.info("Selecting Channel from key");
                SocketChannel channel = (SocketChannel) selectionKey.channel();

                try {
                    channel.close();
                    log.info("Socket Channel closed");

                } catch (IOException e) {
                    log.error("Error occurred while closing socket");
                    e.printStackTrace();
                }

                log.info("Cancelling selection key");
                selectionKey.cancel();

            }

            log.info("Closing the selector");
            try {
                selector.close();
                log.info("Selector closed");
            } catch (IOException e) {
                log.error("Error occurred while closing selector");
            }
        }
    }

}