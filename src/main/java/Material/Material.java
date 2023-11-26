package Material;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.stream.Stream;

public class Material implements Serializable {
    public String name = "";

    public String description = "";;

    public String differentiator = "";;

    public ArrayList<String> tags = new ArrayList<>();

    public double valuePerQty = 0;

    public double overrideValue = 0;

    public double quantity = 0;

    public Instant lifespanStart = Instant.now();

    public long lifespanInSeconds = 0;

    public MaterialComposite composite = new MaterialComposite();

    public Material(String name, String description, String differentiator, ArrayList<String> tags, double valuePerQty,
            double overrideValue, double quantity, Instant lifespanStart, long lifespan, MaterialComposite composite) {
        this.name = name;
        this.description = description;
        this.differentiator = differentiator;
        this.tags = tags;
        this.valuePerQty = valuePerQty;
        this.overrideValue = overrideValue;
        this.quantity = quantity;
        this.lifespanStart = lifespanStart;
        this.lifespanInSeconds = lifespan;
        this.composite = composite;
    }

    public Material() {
    }

    public Material(String name, String description, String differentiator, ArrayList<String> tags, double valuePerQty,
            double overrideValue, double quantity, Instant lifespanStart, long lifespan, ArrayList<Material> composite) {
        this.name = name;
        this.description = description;
        this.differentiator = differentiator;
        this.tags = tags;
        this.valuePerQty = valuePerQty;
        this.overrideValue = overrideValue;
        this.quantity = quantity;
        this.lifespanStart = lifespanStart;
        this.lifespanInSeconds = lifespan;
        this.composite = new MaterialComposite(composite);
    }

    public Integer MaterialID() {
        return (Double.toString(lifespanInSeconds) +
                Double.toString(valuePerQty) +
                Double.toString(overrideValue) +
                differentiator +
                lifespanStart.toString().hashCode() +
                composite.cumulativeID()).hashCode();
    }

    public Integer MaterialIDWithQuantity() {
        return (Double.toString(lifespanInSeconds) +
                Double.toString(valuePerQty) +
                Double.toString(overrideValue) +
                differentiator +
                quantity +
                lifespanStart.toString().hashCode() +
                composite.cumulativeID()).hashCode();
    }

    public Material clone() {
        ArrayList<String> clonedTags = new ArrayList<String>();
        for (String tag : tags) {
            clonedTags.add(String.valueOf(tag));
        }
        return new Material(String.valueOf(name), String.valueOf(description), String.valueOf(differentiator), tags,
                valuePerQty, overrideValue, quantity, Instant.parse(lifespanStart.toString()), lifespanInSeconds, composite);
    }

    public Material loadfrom(byte[] loadfrom) {
        try {
            ObjectInputStream objectInputStream = new ObjectInputStream((InputStream) Stream.of(loadfrom));
            Material material = (Material) objectInputStream.readObject();
            return material;
        } catch (Exception e) {
            return null;

        }
    }

    public boolean isExpired() {
        Instant now = Instant.now();
        return now.isAfter(lifespanStart.plusSeconds(lifespanInSeconds));
    }

    public byte[] serialize() {
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

    public ArrayList<Material> getAllSubMaterials(boolean inclusive, boolean clone) {
        ArrayList<Material> submaterials = new ArrayList<>();
        if (inclusive) {
            if (clone) {
                submaterials.add(clone());
            } else {
                submaterials.add(this);
            }
        }
        composite.materials().forEach(m -> {
            if (clone) {
                submaterials.add(m.clone());
            } else {
                submaterials.add(m);
            }
            submaterials.addAll(m.getAllSubMaterials(false, clone));
        });
        return submaterials;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }

        if (!(o instanceof Material)) {
            return false;
        }

        Material c = (Material) o;

        return c.MaterialID() == MaterialID();
    }

}
