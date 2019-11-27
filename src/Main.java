import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;
import fluffUtil.FluffMetaData;
import fluffUtil.ReadFluffIO;

import javax.print.DocFlavor;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

public class Main {

    public static void main(String[] args) {

        // 1- load data .txt files
        getFiles("/Users/Hesham/dev/fluffReader/resources").filter(f -> f.getFileName().toString().endsWith(".txt")).forEach( filePath -> {
            String data = "";
            String fileName = "";
            try {
                data = new String(Files.readAllBytes(filePath), "UTF-8");
                fileName = filePath.getFileName().toString();
                ReadFluffIO.FluffReader fluffIO = new ReadFluffIO.FluffReader(fileName , data);
                FluffMetaData fluffMetaData = fluffIO.readFluff(true);
                // write content to separate files
                // writeToFile(fileName, fluffMetaData.displayData());


            } catch (IOException e) {
                System.out.println(e.getMessage());
            }

            System.out.println("file name: " + fileName);
//            System.out.println(fluffMetaData.displayData());
            System.out.println("======================================================================");

        });


    }

    static String readFile(String path, Charset encoding)
            throws IOException
    {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }

    static Stream<Path> getFiles(String path) {
        Stream<Path> paths = null;
        try {
            paths = Files.walk(Paths.get(path))
                         .filter(Files::isRegularFile);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return paths;
    }

    static void writeToFile(String fileName, String data) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, true));
        writer.append(data);
        writer.close();
    }
}
