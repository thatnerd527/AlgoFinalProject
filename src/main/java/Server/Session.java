package Server;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.UUID;
import java.util.function.Consumer;

import Server.Inventory.Inventory;
import UI.Menu;

public class Session {
    public Writer outconsole; // User see

    public Reader inconsole; // Read from user

    public String sessionid = UUID.randomUUID().toString();

    public Consumer<IOException> onDisconnect = (e) -> {
    };

    private Writer w;

    private Reader r;

    public WrappedReader wR;

    public WrappedWriter wW;

    private boolean disconnected = false;

    public Session(Writer out, Reader in, Consumer<IOException> onD) {
        onDisconnect = onD;
        w = out;
        r = in;
    }

    public Session startSession() {
        //System.out.println("Session started");
        wR = new WrappedReader(r, (e) -> {
            onDisconnect.accept(e);
            disconnected = true;
        });
        wW = new WrappedWriter(w, (e) -> {
            onDisconnect.accept(e);
            disconnected = true;
        });
        wW.write("If you lose connection, this is your session ID: " + sessionid + "\n");
        while (true) {

            String action = new Menu().withTitle("Main Menu")
                    .withChoice("1", "Inventory")
                    .withChoice("2", "Calculator")
                    .withChoice("3", "Disconnect")
                    .withChoice("4", "Quit")
                    .makeASelection(wW, wR);

            switch (action) {
                case "1":
                    Inventory.HandleInventory(wR, wW);
                    break;

                case "2":

                    break;
                case "3":
                    wR.close();
                    wW.close();
                    break;
                case "4":

                    break;

                default:
                    break;
            }
        }

    }
}
