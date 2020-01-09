package org.narrative.common.web;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: paul
 * Date: Sep 1, 2006
 * Time: 9:50:11 AM
 * To change this template use File | Settings | File Templates.
 */
public class EmbedPrecompiledJsp {

    public static void main(String[] args) throws Exception {
        if (args.length != 3) {
            throw new IllegalArgumentException("You must specify the file to alter, the token to search for and the file to embed");
        }

        StringBuilder inFile = readFile(args[0]);
        StringBuilder repFile = readFile(args[2]);
        String newInFile = inFile.toString().replace(args[1], repFile);

        FileWriter writer = new FileWriter(args[0], false);
        writer.write(newInFile);
        writer.close();

    }

    private static StringBuilder readFile(String filename) throws IOException {
        FileReader reader = new FileReader(filename);
        StringBuilder inFile = new StringBuilder();
        int ch = 0;
        while ((ch = reader.read()) != -1) {
            inFile.append((char) ch);
        }
        reader.close();
        return inFile;
    }
}
