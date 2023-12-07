package Material;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.stream.Stream;

public class MaterialDatabase extends MaterialComposite {
    private static final long serialVersionUID = 

    public synchronized static MaterialDatabase load(byte[] loadfrom) throws IOException, ClassNotFoundException {
        ObjectInputStream objectInputStream = new ObjectInputStream(new ByteArrayInputStream(loadfrom));
        MaterialDatabase material = (MaterialDatabase) objectInputStream.readObject();
        return material;
    }

    public synchronized byte[] save() {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(this);
            oos.close();
            return baos.toByteArray();
        } catch (Exception e) {
            System.out.println("whoops");
            return null;
        }
    }
}
