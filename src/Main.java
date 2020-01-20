import com.sun.tools.javac.util.ArrayUtils;
import fluffUtil.FluffMetaData;
import fluffUtil.ReadFluffIO;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.omg.CosNaming.NamingContextExtPackage.StringNameHelper;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main implements Runnable{
    private Integer countValue = 0;
    AtomicInteger count;

    public Main(Integer countValue) {
       this.countValue = countValue;
       count = new AtomicInteger(countValue);
    }

    private  String getCount() {
        return count.toString();
    }
    public static void main(String[] args) {
//        for (int i = 1; i < 4; i++) {
            Main c = new Main(2);
            c.read();
//        }

        //
//        ExecutorService executor = Executors.newCachedThreadPool();
//        for (int i = 1; i < 9; i++) executor.execute(new Main(i));
        // 1- load data .txt files
//        List<String> fileNames = getFiles("/Users/Hesham/dev/data").map(f -> {
//            String name = f.getFileName().toString();
//            name = name.substring(0, name.length() - ".txt".length());
//            name = name + ".fluff";
//            return name;
//        }).filter(f -> !f.endsWith(".txt")).collect(Collectors.toList());
//        fileNames.add("BC-54-51-01-4B-D7_1558251704000.fluff");
//        fileNames.add("BC-54-51-01-4B-D7_1561608170000.fluff");
//        fileNames.add("BC-54-51-01-4B-D7_1559198051000.fluff");
//        fileNames.add("BC-54-51-01-4B-D7_1553749574000.fluff");
//        fileNames.add("BC-54-51-01-4B-D7_1549974814000.fluff");
//        fileNames.add("BC-54-51-01-4B-D7_1552096928000.fluff");
//        fileNames.add("BC-54-51-01-4B-D7_1553762857000.fluff");
//        fileNames.add("A4-6C-F1-1C-9D-01_1553639251000.fluff");
//        fileNames.add("BC-54-51-01-4B-D7_1556252721000.fluff");
//        fileNames.add("BC-54-51-01-4B-D7_1551767504000.fluff");

//        System.out.println(fileNames.size());
//        System.out.println(getFiles("/Users/Hesham/dev/fluffReader/resources/Pat5").count());



        // edit file names to be timely sorted
//        getFiles("/Users/Hesham/dev/data").forEach(f -> {
//            String[] name = f.getFileName().toString().split("_");
//            name[1] = name[1].substring(0, name[1].length() - ".txt".length());
//            f.toFile().renameTo(new File(name[1] + "_" + name[0] + ".txt"));
//        });

    }

    private static String print(String[] sensorSpecs, String[] lables) {
        StringBuilder res = new StringBuilder();
        for (int i = 0; i < sensorSpecs.length; i++) {
            res.append(lables[i]);
            res.append(":");
            res.append(sensorSpecs[i]);
            if (i != sensorSpecs.length -1) {
                res.append(":");
                res.append("\n");
            }
        }
        return res.toString();
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
        fileName = fileName.substring("ST-1946093440_SENSOR_".length(), fileName.length() - ".fluff.json".length()) + ".txt";
        BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, true));
        writer.append(data);
        writer.close();
    }

    @Override
    public void run() {
        read();
    }
//
    public void read() {
//        HashSet<String> specs = new HashSet<>();

//        getFiles("/Users/Hesham/dev/fluffReader" ).forEach( f -> {
        getFiles("/Volumes/hex/rest/" + getCount() ).forEach( f -> {
//        getFiles("/Volumes/hex/rest").forEach( f -> {

            String fileName = f.getFileName().toString();
            if (fileName.endsWith("json")) {

                try {
                    //                if (!fileNames.contains(fileName.substring(0, fileName.length() - ".fluff".length()))) {
                    //                if (!fileNames.contains(fileName)) {


                    //                InputStream data = new FileInputStream(f.toFile());
                    String[] things = new String(Files.readAllBytes(f), "UTF-8").split("'");
                    // for reading .json
                    ReadFluffIO.FluffReader fluffIO = new ReadFluffIO.FluffReader(fileName, things[things.length - 2]);
                    // for reading .fluff
                    //                ReadFluffIO.FluffReader fluffIO = new ReadFluffIO.FluffReader(fileName, data);

                    FluffMetaData fluffMetaData = fluffIO.readFluff(true);
                    //                specs.add(print(fluffMetaData.sensorSpecs, fluffMetaData.sensorLabels));
                    //                System.out.println(print(fluffMetaData.sensorSpecs, fluffMetaData.sensorLabels));

                    // write content to separate files
                    writeToFile(fileName, fluffMetaData.displayData());

                    //                System.out.println(": file name: " + fileName);
                    //                System.out.println("======================================================================");


                } catch (OutOfMemoryError memoryError) {
                    System.out.println(fileName + " memory error");
                } catch (IllegalArgumentException e) {
                    System.out.println("illegal argument in file " + f.getFileName());
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                } catch (ArrayIndexOutOfBoundsException a) {
                    System.out.println(this.getCount());
                    System.out.println(fileName);
                    System.out.println(a.getMessage());
                }
            }
        });

//        try {
//            String  s = "";
//            for(String i : specs ) {
//                s += i;
//            }
//            writeToFile("sensorSpec", s);
//        } catch (IOException e) {
//
//        }
    }

}
