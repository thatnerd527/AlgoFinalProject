package Material;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
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

    public Date lifespanStart = new Date(0);

    public double lifespan = 0;

    public MaterialComposite composite = new MaterialComposite();

    public Material(String name, String description, String differentiator, ArrayList<String> tags, double valuePerQty,
            double overrideValue, double quantity, Date lifespanStart, double lifespan, MaterialComposite composite) {
        this.name = name;
        this.description = description;
        this.differentiator = differentiator;
        this.tags = tags;
        this.valuePerQty = valuePerQty;
        this.overrideValue = overrideValue;
        this.quantity = quantity;
        this.lifespanStart = lifespanStart;
        this.lifespan = lifespan;
        this.composite = composite;
    }



    public Material() {
    }

    public Material(String name, String description, String differentiator, ArrayList<String> tags, double valuePerQty,
            double overrideValue, double quantity, Date lifespanStart, double lifespan, ArrayList<Material> composite) {
        this.name = name;
        this.description = description;
        this.differentiator = differentiator;
        this.tags = tags;
        this.valuePerQty = valuePerQty;
        this.overrideValue = overrideValue;
        this.quantity = quantity;
        this.lifespanStart = lifespanStart;
        this.lifespan = lifespan;
        this.composite = new MaterialComposite(composite);
    }

    public Integer MaterialID() {
        return (Double.toString(lifespan) +
                Double.toString(valuePerQty) +
                Double.toString(overrideValue) +
                differentiator +
                lifespanStart.toString().hashCode()+
                composite.cumulativeID()).hashCode();
    }

    public Integer MaterialIDWithQuantity() {
        return (Double.toString(lifespan) +
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
                valuePerQty, overrideValue, quantity, Date.from(lifespanStart.toInstant()), lifespan, composite);
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
