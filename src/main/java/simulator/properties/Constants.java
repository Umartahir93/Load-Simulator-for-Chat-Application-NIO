package simulator.properties;

public class Constants {

    public static final int SERVER_SOURCE_ID = 0;
    public static final int NO_MASSAGE_LENGTH_DEFINED = 0;
    public static final String LOGIN_MESSAGE = "";
    public static final String LOGOUT_MESSAGE = "";


    public static final int START_OF_MAGIC_BYTES_INCLUSIVE = 0;
    public static final int END_OF_MAGIC_BYTES_EXCLUSIVE = 4;
    public static final int START_OF_MESSAGE_TYPE_INCLUSIVE = 4;
    public static final int END_OF_MESSAGE_TYPE_EXCLUSIVE = 6;
    public static final int START_OF_SOURCE_ID_INCLUSIVE = 6;
    public static final int END_OF_SOURCE_ID_EXCLUSIVE = 10;
    public static final int START_OF_DEST_ID_INCLUSIVE = 10;
    public static final int END_OF_DEST_ID_EXCLUSIVE = 14;
    public static final int START_OF_MESSAGE_LENGTH_INCLUSIVE = 14;
    public static final int END_OF_MESSAGE_LENGTH_EXCLUSIVE = 18;
    public static final int START_OF_MESSAGE_INCLUSIVE = 18;

    public static final int EXIT_PROGRAM_VALUE  = 0;
    public static final int LOGIN_PROGRAM_VALUE = 1;


}
