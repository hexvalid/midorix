package com.midorix.pipos;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Helpers {

    protected static void pinSwitcher(File file, boolean state) throws IOException {
        FileWriter writer = new FileWriter(file);
        if (state) {
            writer.append('1');
        } else {
            writer.append('0');
        }
        writer.close();
    }
}
