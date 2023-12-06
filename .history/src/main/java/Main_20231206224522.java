import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import Material.MaterialBuilder;
import Material.MaterialDatabase;
import Server.InterruptibleReader;
import Server.Server;
import Server.Session;
import Server.WrappedReader;
import Server.WrappedWriter;
import UI.InputForm;
import UI.Table;

class Main {

  public static void Connect(String host, String sessionid) {
    try {
      Socket socket = new Socket(host, 6868); // Replace 12345 with the desired port number

      // Read input from system.in and send it to the socket
      InterruptibleReader rdr = new InterruptibleReader(System.in);
      Thread t = new Thread(() -> {

        try (BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))) {

          out.write(sessionid);
          out.newLine();
          out.flush();

          String strread = "";
          while (!strread.startsWith("\0")) {
            strread = rdr.readLine();
            System.out.println(strread.length());
            out.write(strread.trim());
            out.newLine();
            out.flush();
          }

          socket.close();
        } catch (IOException e) {

        }
      });
      t.start();

      try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
        String serverResponse;
        while ((serverResponse = in.readLine()) != null) {
          System.out.println(serverResponse);
        }
        socket.close();
      } catch (IOException e) {
      }
      rdr.interrupt();
      // Read output from the socket and print it to system.out

    } catch (IOException e) {

    }
  }

  public static void main(String[] args) {

    if (args.length > 0) {
      if (args[0].equals("server")) {

        Server.StaticInit();
        Connect("localhost", "none");
        return;
      }
    }
    Server.StaticInit();
    Connect("localhost", "none");

    while (true) {

      HashMap<String, String> res = new InputForm(new WrappedWriter(new OutputStreamWriter(System.out)),
          new WrappedReader(new InputStreamReader(System.in)))
          .withTitle("Connect to a node")
          .withField("IP Address / Host", true)
          .withField("Session ID", false)
          .receiveInput();
      Connect(res.get("IP Address / Host"), res.get("Session ID"));

    }

    // System.out.print(Table.getPrintedTable(testtable, true));

    // Serialization test

    /*
     * MaterialDatabase dat = new MaterialDatabase();
     * dat.addMaterial(new MaterialBuilder().build());
     * dat.save();
     * 
     * Session ses = new Session(new OutputStreamWriter(System.out), new
     * InputStreamReader(System.in), (e) -> {
     * System.out.println(e.getMessage());
     * });
     * ses.startSession();
     */

  }
}