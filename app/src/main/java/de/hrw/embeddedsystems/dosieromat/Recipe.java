package de.hrw.embeddedsystems.dosieromat;

import android.content.res.XmlResourceParser;
import android.os.Debug;
import android.provider.DocumentsContract;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class Recipe {
    private String name;
    private List<Ingredient> ingredients;
    private int amount;
    private String unit;

    public Recipe(String name, int amount, String unit, List<Ingredient> ingredients) {
        this.name = name;
        this.amount = amount;
        this.unit = unit;
        this.ingredients = ingredients;
    }

    @NonNull
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append(String.format(Locale.GERMANY, "Rezept: %1$d %2$s %3$s:", amount, unit, name));
        sb.append('\n');

        for (Ingredient i : ingredients) {
            sb.append(i.toString());
            sb.append('\n');
        }

        return sb.toString();
    }

    public List<String> toCommandList(int numOfPortions) {
        List<String> commands = new ArrayList<>();

        for (Ingredient i : ingredients) {
            if(i.isAutoDispensed()) {
                StringBuilder sb = new StringBuilder();
                sb.append(i.getName());
                sb.append(';');
                sb.append(i.getAmount() * numOfPortions);
                commands.add(sb.toString());
            }
        }

        return commands;
    }
}
