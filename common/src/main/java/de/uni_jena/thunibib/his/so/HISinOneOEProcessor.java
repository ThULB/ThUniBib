package de.uni_jena.thunibib.his.so;

import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;

/**
 * @author shermann (Silvio Hermann)
 * */
public class HISinOneOEProcessor {

    private static final String DELIMITER = ";";

    public static void csvToXml(String in, String out) throws Exception {
        try (Stream<String> l = Files.lines(Paths.get(in))) {
            String[] lines = l.toArray(String[]::new);

            String[] heading = lines[0].split(DELIMITER);
            SOBuilder soBuilder = null;

            for (int i = 1; i < lines.length; i++) {
                String[] line = lines[i].split(DELIMITER);

                int plid;
                if ("NULL".equals(line[13])) {
                    plid = -1;
                } else {
                    plid = Integer.parseInt(line[13]);
                }
                SOUnit newUnit = new SOUnit(Integer.valueOf(Integer.parseInt(line[1])), plid);
                if (i == 1) {
                    soBuilder = new SOBuilder(newUnit);
                }

                for (int j = 0; j < line.length; j++) {
                    if (j < heading.length) {
                        String h = heading[j];
                        String v = line[j];
                        newUnit.addAttribute(h, v);
                    }
                }
                soBuilder.add(newUnit);
            }

            try (FileOutputStream fos = new FileOutputStream(new File(out))) {
                new XMLOutputter(Format.getPrettyFormat()).output(soBuilder.toMCRClassification(), fos);
            }
        }
    }
}
