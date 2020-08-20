package simulator.utility;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.function.Consumer;

@Slf4j
public class InputValidator {

    public static Consumer<String> commandLineArguments(){
        return input -> {
            if (!StringUtils.isNumeric(input)) {
                log.info("Please provide port number as command line argument");
                System.exit(0);
            }
        };
    }
}
