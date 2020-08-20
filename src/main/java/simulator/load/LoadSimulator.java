package simulator.load;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import simulator.core.InternalCore;
import simulator.utility.InputValidator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;


@Slf4j
public class LoadSimulator {

    //shared between different threads
    @Getter
    private static final Map<String,Long> connectionAcceptanceTime = new ConcurrentHashMap<>();

    //shared between different threads
    @Getter
    private static final List<Integer> clientsConnectedToUser = new ArrayList<>();

    public static void main(String[] args) {
        try {

            log.info("=== Please mention number of clients needed for simulation===");
            Scanner scanner = new Scanner(System.in);
            String clients = scanner.nextLine();
            InputValidator.commandLineArguments().accept(clients);

            Runnable runnable =
                    () ->{
                        InternalCore client = new InternalCore(5000, "localhost");
                        client.initiateApplication();
                    };

            for(int i = 0; i <Integer.parseInt(clients); i++){
                Thread thread = new Thread(runnable);
                thread.start();
            }


        } catch (Exception exception) {
            log.error("Connection lost with server");
            log.error("Cause of error ", exception);
        }
    }
}