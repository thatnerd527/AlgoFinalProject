package Material;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.stream.Stream;

public class MaterialDatabase extends MaterialComposite {
    public static MaterialDatabase load(byte[] loadfrom) {
        try {
            ObjectInputStream objectInputStream = new ObjectInputStream((InputStream) Stream.of(loadfrom));
            MaterialDatabase material = (MaterialDatabase) objectInputStream.readObject();
            return material;
        } catch (Exception e) {
            return null;

        }
    }

    public byte[] save() {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(this);
            oos.close();
            return baos.toByteArray();
        } catch (Exception e) {
            return null;
        }
    }
}
