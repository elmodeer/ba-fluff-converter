import fluffUtil.FluffMetaData;
import fluffUtil.ReadFluffIO;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {

    public static void main(String[] args) {

        // 1- load data .txt files
        List<String> fileNames = getFiles("/Users/Hesham/dev/fluffReader/data").map(f -> f.getFileName().toString()).collect(Collectors.toList());
        System.out.println(fileNames.size());
        System.out.println(getFiles("/Users/Hesham/dev/fluffReader/resources/Pat5").count());
        getFiles("/Users/Hesham/dev/fluffReader/resources/Pat5").forEach( f -> {
            String fileName = f.getFileName().toString();
            try {
                if (!fileNames.contains(fileName.substring(0, fileName.length() - ".fluff".length()))) {
                    InputStream data = new FileInputStream(f.toFile());
                    ReadFluffIO.FluffReader fluffIO = new ReadFluffIO.FluffReader(fileName, data);
                    FluffMetaData fluffMetaData = fluffIO.readFluff(true);
                    // write content to separate files
                    writeToFile(fileName, fluffMetaData.displayData());
                    System.out.println(": file name: " + fileName);
                    System.out.println("======================================================================");
                }

            } catch (OutOfMemoryError memoryError) {
                System.out.println(fileName + " memory error");
            }
            catch (IOException e) {
                System.out.println(e.getMessage());
            }
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
        fileName = fileName.substring(0, fileName.length() - ".fluff".length());
        BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, true));
        writer.append(data);
        writer.close();
    }
}
