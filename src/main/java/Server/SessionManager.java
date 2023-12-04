package Server;


import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Optional;

public class SessionManager {
    private static ArrayList<Session> sessions = new ArrayList<Session>();

    public static Session joinByString(String str, Reader r, Writer w) {
        Optional<Session> session = sessions.stream().filter(s -> s.sessionid.toString().equals(str)).findFirst();
        if (session.isPresent()) {
            session.get().wR._reader = r;
            session.get().wW._writer = w;
            return session.get();
        } else {
            sessions.add(new Session(w, r, (e) -> {
                try {
                    Thread.sleep(4000);
                } catch (InterruptedException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
            }));
            new Thread(() -> {
                sessions.get(sessions.size() - 1).startSession();
            }).start();
            return sessions.get(sessions.size() - 1);
        }

    }
}
