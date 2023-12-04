package Server;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

import Material.MaterialDatabase;

public class Server {
    public static MaterialDatabase currentlystored = new MaterialDatabase();
    public static MaterialDatabase templatematerials = new MaterialDatabase();

    static byte[] readAsBytes(File file) throws IOException {
        int length = (int) file.length();
        char[] output = new char[length];
        FileReader fr = new FileReader(file);
        fr.read(output);
        fr.close();
        return new String(output).getBytes();
    }

    static void saveToWriter(FileWriter wr, byte[] arr) throws IOException {
        wr.write(new String(arr).toCharArray());
    }

    static void StartServer() {
        new Thread(() -> {
            try {
                ServerSocket serverSocket = new ServerSocket(6868);
                while (true) {
                    Socket client = serverSocket.accept();

                    // get sesison id
                    String session = "";
                    Scanner a = new Scanner(client.getInputStream());
                    try {
                        session = a.nextLine();
                        System.out.println("Session id: " + session);
                        SessionManager.joinByString(session,new InputStreamReader(client.getInputStream()), new OutputStreamWriter(client.getOutputStream()));
                    } catch (IOException e) {
                        System.out.println("Could not get session id.");
                    }

                }
            } catch (IOException e) {
                System.out.println("Server closed.");
            }
        }).start();
    }

    public static void StaticInit() {
        System.out.println("Starting as server.");
        try {
            currentlystored = MaterialDatabase.load(readAsBytes(new File(("./current.bin"))));
            templatematerials = MaterialDatabase.load(readAsBytes(new File("./template.bin")));

            StartServer();
        } catch (Exception e) {
            try {
                currentlystored = new MaterialDatabase();
                templatematerials = new MaterialDatabase();
                saveToWriter(new FileWriter("./current.bin"), currentlystored.save());
                saveToWriter(new FileWriter("./template.bin"), templatematerials.save());
                StartServer();
            } catch (IOException e2) {
            }
        }
    }

    public synchronized static void SaveAll() {
        try {

            FileWriter cur = new FileWriter("./current.bin");
            FileWriter template = new FileWriter("./template.bin");

            saveToWriter(cur, currentlystored.save());
            saveToWriter(template, templatematerials.save());
        } catch (Exception e) {

        }
    }
}
