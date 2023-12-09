package Material;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class MaterialComposite implements Serializable {
    protected ArrayList<Material> materials;

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

    

    public synchronized byte[] save() {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            XMLEncoder encoder = new XMLEncoder(baos);
            encoder.writeObject(this);
            encoder.flush();
            encoder.close();
            baos.flush();
            baos.close();
            return baos.toByteArray();
        } catch (Exception e) {
            System.out.println("whoops");
            return null;
        }
    }

    public synchronized static MaterialComposite load(byte[] loadfrom) throws IOException, ClassNotFoundException {
        XMLDecoder decoder =
            new XMLDecoder(new ByteArrayInputStream(loadfrom));
        return (MaterialComposite) decoder.readObject();
    }

    public Long cumulativeID() {
        long cumulativeID = 0;
        for (Material material : materials) {
            cumulativeID += material.MaterialID();
        }
        return cumulativeID;
    }

    public ArrayList<Material> materials() {
        return new ArrayList<>(materials);
    }

    public ArrayList<Material> materialsSub() {
        MaterialComposite comp = new MaterialComposite();
        new ArrayList<Material>(
                materials.stream()
                        .map((Material x) -> x.getAllSubMaterials(true, true))
                        .flatMap(List::stream)
                        .collect(Collectors.toList()))
                .forEach((Material material) -> {
                    comp.addMaterial(material);
                });
        return comp.materials();

    }

    public Material isPresentOr0(Material material) {
        for (Material m : materials) {
            if (m.MaterialID() == material.MaterialID()) {
                return m;
            }
        }
        Material clone = material.clone();
        clone.quantity = 0;
        return clone;
    }

    public boolean contains(Material material) {
        return isPresentOr0(material).quantity >= material.quantity;
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

    public MaterialComposite() {
        this.materials = new ArrayList<Material>();
    }
}
