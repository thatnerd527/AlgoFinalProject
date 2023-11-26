package UI;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import Server.WrappedReader;
import Server.WrappedWriter;

public class Menu {

    public String title = "Menu";

    public HashMap<String, String> choices = new HashMap<String, String>();

    public ArrayList<String> getIndexes() {
        ArrayList<String> result = new ArrayList<>();
        choices.keySet().forEach(x -> {
            result.add(x);
        });
        return result;
    }

    public ArrayList<String> getValues() {
        ArrayList<String> result = new ArrayList<>();
        choices.values().forEach(x -> {
            result.add(x);
        });
        return result;
    }

    public Menu withChoice(String index, String value) {
        choices.put(index, value);
        return this;
    }

    public Menu withTitle(String title) {
        this.title = title;
        return this;
    }

    public static String stringGenerator(String txt, Integer amount) {
        String result = "";
        for (int i = 0; i < amount; i++) {
            result += txt;
        }
        return result;
    }

    public String build() {
        StringBuilder built = new StringBuilder();
        int longest = Integer
                .parseInt(choices.keySet().stream().reduce("0",
                        (String currentlength, String el) -> ((Integer) Math
                                .max(("| " + el + " = " + choices.get(el)).length(), Integer.parseInt(currentlength)))
                                .toString()));
        built.append("| " + title + "\n");
        built.append(stringGenerator("=", longest) + "\n");
        choices.keySet().stream().forEach((String el) -> {
            built.append("| " + el + " = " + choices.get(el) + "\n");
        });
        built.append(stringGenerator("=", longest) + "\n");
        return built.toString();
        // return title + "\n" + "==================="
    }

    public String makeASelection(WrappedWriter out, WrappedReader read) {

        Scanner scanner = new Scanner(read);
        while (true) {

            out.write(build());
            out.write("Make a selection: ");
            String choice = scanner.nextLine();
            if (!getIndexes().contains(choice)) {
                out.write("That is not an option. \n");
                continue;
            }
            //scanner.close();
            return choice;
        }
    }
}
