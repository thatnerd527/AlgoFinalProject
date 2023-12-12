package Material;

import java.beans.XMLDecoder;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;

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

    @Override
    public void setMaterials(ArrayList<Material> materials) {
        this.materials.clear();
        for (Material material : materials) {
            this.addMaterial(material);
        }
    }

    public MaterialDatabase() {
        this.materials = new ArrayList<Material>();
    }
}
