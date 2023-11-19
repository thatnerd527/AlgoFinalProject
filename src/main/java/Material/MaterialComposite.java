package Material;

import java.io.Serializable;
import java.util.ArrayList;
public class MaterialComposite implements Serializable {
    private ArrayList<Material> materials = new ArrayList<Material>();

    public void addMaterial(Material material) {
        // Find with the same id
        for (int i = 0; i < materials.size(); i++) {
            if (materials.get(i).MaterialID() == material.MaterialID()) {
                materials.get(i).quantity += material.quantity;
                return;
            }
        }
        materials.add(material);
    }

    public void removeMaterial(Material material) {
        for (int i = 0; i < materials.size(); i++) {
            if (materials.get(i).MaterialID() == material.MaterialID()) {
                materials.get(i).quantity -= material.quantity;
                if (materials.get(i).quantity <= 0) {
                    materials.remove(i);
                }
                return;
            }
        }
        throw new RuntimeException("Material not found");
    }

    public void removeMaterial(int id, double quantity) {
        for (int i = 0; i < materials.size(); i++) {
            if (materials.get(i).MaterialID() == id) {
                if (quantity <= 0) {
                    materials.remove(i);
                    return;
                }
                materials.get(i).quantity -= quantity;
                if (materials.get(i).quantity <= 0) {
                    materials.remove(i);
                }
                return;
            }
        }
        throw new RuntimeException("Material not found");
    }

    public Long cumulativeID() {
        long cumulativeID = 0;
        for (Material material : materials) {
            cumulativeID += material.MaterialID();
        }
        return cumulativeID;
    }

    public ArrayList<Material> materials() {
        return materials;
    }

    public MaterialComposite clone() {
        ArrayList<Material> materials_cloned = new ArrayList<Material>();
        for (Material material : materials) {
            materials_cloned.add(material.clone());
        }
        return new MaterialComposite(materials_cloned);
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }

        if (!(o instanceof Material)) {
            return false;
        }

        MaterialComposite c = (MaterialComposite) o;

        return cumulativeID() == c.cumulativeID();
    }

    public MaterialComposite(ArrayList<Material> materials) {
        for (Material material : materials) {
            this.addMaterial(material);
        }
    }

    public MaterialComposite() {
    }
}
