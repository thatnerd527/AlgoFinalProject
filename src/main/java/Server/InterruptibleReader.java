package Server;

import java.io.Reader;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;

public class InterruptibleReader {

    public InputStream r = null;

    public InterruptibleReader(InputStream r) {
        this.r = r;
    }

    private boolean returned = false;

    private Thread locked = null;

    private Thread reader = null;

    private boolean manual = false;

    public String readLine() {
        StringBuilder sb = new StringBuilder();
        locked = Thread.currentThread();
        reader = new Thread(() -> {

            try {
                byte[] b = new byte[1];
                InterruptibleInputStream rdr2 = new InterruptibleInputStream(r);

                while (rdr2.read(b) != -1) {

                    String cast = new String(b);
                    //System.out.println(cast);
                    if (cast.startsWith("\n") || cast.startsWith("\r")) {
                        break;
                    }
                    sb.append(cast);
                }

            } catch (IOException e) {
                returned = true;
                sb.append("\0");
                locked.interrupt();
            }
            if (!manual && !returned) {
                returned = true;
                locked.interrupt();
            }
        });
        try {
            reader.start();
            manual = false;
            returned = false;
            while (true) {
                Thread.sleep(Long.MAX_VALUE);
            }
        } catch (InterruptedException e) {
            return sb.toString();
        }
    }

    public void interrupt() {
        if (!manual && !returned) {
            manual = true;
            System.out.println("1111");
            locked.interrupt();
            reader.interrupt();
        }
    }

}
