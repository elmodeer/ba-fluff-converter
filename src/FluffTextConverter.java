import com.sun.tools.jdi.IntegerTypeImpl;
import fluffUtil.FluffMetaData;
import fluffUtil.ReadFluffIO;
import sun.jvm.hotspot.utilities.AssertionFailure;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FluffTextConverter implements Runnable {
    List<File> files;
    String outputDir;
    public FluffTextConverter(List<File> files, String outputDir) {
        this.outputDir = outputDir;
        if (files != null)
            this.files = files;
        else
            throw new NullPointerException("null stream");
    }


    public static void main(String[] args) {


        // prepare lists for multithreading. a workaround in order not to read same files in concurrent threads
        String patientCode = "ST1814523348";

        String outputDirectory = "/Volumes/hex/" + patientCode + "-txt";
        new File(outputDirectory).mkdir();

        String mainPatientDirectory = "/Volumes/hex/" + patientCode;

        List<List<File>> partitions = new ArrayList<>();
        List<File> allFiles = null;
        try {
            allFiles = Files.walk(Paths.get(mainPatientDirectory))
                            .filter(Files::isRegularFile)
                            .map(f -> f.toFile()).collect(Collectors.toList());
        } catch (IOException e ){
            System.out.println("can't read files from directory " + mainPatientDirectory);
        }

        int filesLength = allFiles.size();
        int start = 0;
        int partitionSize = 300;
        int end = partitionSize;
        boolean endReached = false;
//        TODO!! rewrite in more human way
//        it just checks for the end of the files list to stop sub-listing
        while(!endReached) {
            if ((end - start) < partitionSize) {
                endReached = true;
            }

            partitions.add(allFiles.subList(start, end));

            start = end;
            if ((end + partitionSize) < filesLength)
                end  += partitionSize;
            else {
                end = filesLength;
            }
        }
        System.out.println("Initiating " + partitions.size() + " threads with partition size of: " + partitionSize);
        ExecutorService executor = Executors.newCachedThreadPool();
        // single thread
//        executor.execute(new FluffTextConverter(allFiles, outputDirectory));
        // multi-thread
        for (List<File> partition: partitions) {
            System.out.println("new Thread started");
            executor.execute(new FluffTextConverter(partition, outputDirectory));
        }
    }

    private static String getString(String[] sensorSpecs, String[] lables) {
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

    private void writeToFile(String fileName, String data) throws IOException {
        String patientName = "ST1814523348";
        fileName = fileName.substring(patientName.length() + "_SENSOR_".length(), fileName.length() - ".fluff.json".length()) + ".txt";
        String newFileName = outputDir + "/" + fileName ;

        BufferedWriter writer = new BufferedWriter(new FileWriter(newFileName, true));
        writer.append(data);
        writer.close();
    }

    public static Stream<Path> getSlice(Stream<Path> stream, long fromIndex, long toIndex) {
        return stream.skip(fromIndex)
                     .limit(toIndex - fromIndex + 1);
    }

    @Override
    public void run() {
        read();
    }

    public void read() {
//        HashSet<String> specs = new HashSet<>();
        files.forEach( f -> {
            String fileName = f.getName();
            if (fileName.endsWith("json")) {

                try {
//                    for reading .json
                    String[] things = new String(Files.readAllBytes(Paths.get(f.toURI())), "UTF-8").split("'");
                    String data = things[things.length - 2];
                    ReadFluffIO.FluffReader fluffIO = new ReadFluffIO.FluffReader(fileName, data);
//                    for reading .fluff
//                    InputStream data = new FileInputStream(f.toFile());
//                    ReadFluffIO.FluffReader fluffIO = new ReadFluffIO.FluffReader(fileName, data);

                    FluffMetaData fluffMetaData = fluffIO.readFluff(true);
//                    specs.add(getString(fluffMetaData.sensorSpecs, fluffMetaData.sensorLabels));
//                    System.out.println(print(fluffMetaData.sensorSpecs, fluffMetaData.sensorLabels));

                    // write content to separate files
                    writeToFile(fileName, fluffMetaData.displayData());

                } catch (OutOfMemoryError memoryError) {
                    System.out.println(fileName + " memory error");
                } catch (IllegalArgumentException e) {
                    System.out.println(fileName + "illegal argument in file ");
                } catch (IOException e) {
                    System.out.println(fileName + e.getMessage());
                } catch (ArrayIndexOutOfBoundsException a) {
                    System.out.println(fileName);
                    System.out.println(a.getMessage());
                }
            }
        });
        System.out.println("thread finished");

//        try {
//            String  content = "";
//            for(String i : specs ) {
//                content += i;
//            }
//            BufferedWriter writer = new BufferedWriter(new FileWriter("sensorsSpec.txt", true));
//            writer.append(content);
//            writer.close();
//        } catch (IOException e) {
//            System.out.println("problem in sensorsSpec file");
//        }
    }

}
