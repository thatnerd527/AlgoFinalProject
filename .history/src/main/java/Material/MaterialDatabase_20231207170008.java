package Material;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.stream.Stream;

public class MaterialDatabase extends MaterialComposite {
    private static final long serialVersionUID = 6529685098267757690L;

    public synchronized static MaterialDatabase load(byte[] loadfrom) throws IOException, ClassNotFoundException {
        XMLDecoder decoder =
            new XMLDecoder(new ByteArrayInputStream(loadfrom));
        return (MaterialDatabase) decoder.readObject();
    }

    public MaterialDatabase(ArrayList<Material> materials) {
        this.materials = new ArrayList<Material>();
        for (Material material : materials) {
            this.addMaterial(material);
        }
    }

    public ArrayList<Material> getMaterials() {
        return this.materials();
    }

    public void setMaterials(ArrayList<Material> materials) {
        for (Material material : materials) {
            this.addMaterial(material);
        }
    }

    public MaterialDatabase() {
        this.materials = new ArrayList<Material>();
    }
}
