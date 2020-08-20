package simulator.domain;

import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;

@Getter
public enum MessageType {
    LOGIN("LI"),
    LOGOUT("LO"),
    GENERATED_ID("ID"),
    DATA("DT");

    private final String messageCode;

    MessageType(String code) {
        this.messageCode = code;
    }

    public static Optional<MessageType> fromTextGetMessageType(String text) {
        return Arrays.stream(values()).
                filter(messageType -> messageType.messageCode.equalsIgnoreCase(text))
                .findFirst();
    }
}
