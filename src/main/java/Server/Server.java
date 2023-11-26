package Server;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

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

    public static void StaticInit() {
        try {
            currentlystored = MaterialDatabase.load(readAsBytes(new File(("./current.bin"))));
            templatematerials = MaterialDatabase.load(readAsBytes(new File("./template.bin")));

        } catch (IOException e) {
            try {
                saveToWriter(new FileWriter("./current.bin"), currentlystored.save());
                saveToWriter(new FileWriter("./template.bin"), templatematerials.save());
            } catch (IOException e2) {
            }
        }
    }

    public static void SaveAll() {
        try {

            FileWriter cur = new FileWriter("./current.bin");
            FileWriter template = new FileWriter("./template.bin");

            saveToWriter(cur, currentlystored.save());
            saveToWriter(template, templatematerials.save());
        } catch (Exception e) {

        }
    }
}
