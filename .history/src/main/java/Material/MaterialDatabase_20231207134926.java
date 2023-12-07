package Material;

import java.beans.XMLDecoder;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.stream.Stream;

public class MaterialDatabase extends MaterialComposite {
    private static final long serialVersionUID = 6529685098267757690L;

    public synchronized static MaterialDatabase load(byte[] loadfrom) throws IOException, ClassNotFoundException {
        XMLDecoder decoder = new XMLDecoder(
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
