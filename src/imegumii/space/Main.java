package imegumii.space;

import java.util.logging.Logger;

public class Main {

    public final static String LOGTAG = "imegumii.space";
    public final static String NEWLINE = "\n";

    public static void main(String[] args)
    {
        new Server().handleConnections();

    }
}
