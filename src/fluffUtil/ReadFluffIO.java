package fluffUtil;

import com.sun.istack.internal.NotNull;
import org.apache.log4j.Logger;

//import javax.validation.constraints.NotNull;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.*;

public class ReadFluffIO {

    //================================================================================
    // Internal classes used to interpret fluff data -> provided by InfAI
    //================================================================================

    public static class FluffReader {

        private static final int BUFFER_SIZE = 10000;
        private final PushbackInputStream mPIS;
//        private final Logger log;
        private final FluffMetaData metaData;
        private File mFile;

        public FluffReader(String fluffName, InputStream data) {
//            this.log = log;
            mPIS = new PushbackInputStream(data, BUFFER_SIZE);
            metaData = new FluffMetaData(fluffName);
        }

        public FluffReader(String fluffName, String base64Data) {
//            this.log = log;
            metaData = new FluffMetaData(fluffName);
            mPIS = new PushbackInputStream(new ByteArrayInputStream(Base64.getDecoder().decode(base64Data)), BUFFER_SIZE);
        }

        public FluffMetaData readFluff(boolean includeData) {
            readHeader();
            if (includeData) {
                ReadData();
            }
            return metaData;
        }

        private synchronized void readHeader() {
            try {
                //reset to beginning of file
                //mPIS.reset();

                byte[] buf;

                //read length of Header in bytes 1xint32
                buf = new byte[4];
                mPIS.read(buf, 0, 4);
                metaData.headerLen = IO.bytesToInt(buf);

                //read # bytes of data-type-id
                buf = new byte[4];
                mPIS.read(buf, 0, 4);
                metaData.dataIDLen = IO.bytesToInt(buf);

                //read # data types
                buf = new byte[4];
                mPIS.read(buf, 0, 4);
                metaData.nDataTypes = IO.bytesToInt(buf);

                //read primitive data type specification
                metaData.dataTypes = new String[metaData.nDataTypes];
                for (int i = 0; i < (metaData.nDataTypes); i++) {
                    mPIS.skip(metaData.dataIDLen);
                    buf = new byte[2];
                    mPIS.read(buf, 0, 2);
                    int len = IO.bytesToShort(buf);
                    buf = new byte[len];
                    mPIS.read(buf, 0, len);
                    metaData.dataTypes[i] = IO.bytesToString(buf);
                }

                //read # of sensors 1xint32
                buf = new byte[4];
                mPIS.read(buf, 0, 4);
                metaData.nSensors = IO.bytesToInt(buf);

                //calculate how many byte are necessary to encode sensor-ID
                metaData.nBytesSID = (int) Math.ceil(Math.log10(metaData.nSensors) / Math.log10(2) / 8);

                //read start-time: year(int32),month(int32),day(int32),hour(int32),min(int32),s(int32),ms(int32),us(int32),ns(int32)
                buf = new byte[4];
                metaData.startTime = new int[9];
                mPIS.read(buf, 0, 4);
                metaData.startTime[0] = IO.bytesToInt(buf);
                mPIS.read(buf, 0, 4);
                metaData.startTime[1] = IO.bytesToInt(buf);
                mPIS.read(buf, 0, 4);
                metaData.startTime[2] = IO.bytesToInt(buf);
                mPIS.read(buf, 0, 4);
                metaData.startTime[3] = IO.bytesToInt(buf);
                mPIS.read(buf, 0, 4);
                metaData.startTime[4] = IO.bytesToInt(buf);
                mPIS.read(buf, 0, 4);
                metaData.startTime[5] = IO.bytesToInt(buf);
                mPIS.read(buf, 0, 4);
                metaData.startTime[6] = IO.bytesToInt(buf);
                mPIS.read(buf, 0, 4);
                metaData.startTime[7] = IO.bytesToInt(buf);
                mPIS.read(buf, 0, 4);
                metaData.startTime[8] = IO.bytesToInt(buf);

                //read end-time of measurement (-1) will be replaced when measurement is done)
                buf = new byte[4];
                metaData.endTime = new int[9];
                mPIS.read(buf, 0, 4);
                metaData.endTime[0] = IO.bytesToInt(buf);
                mPIS.read(buf, 0, 4);
                metaData.endTime[1] = IO.bytesToInt(buf);
                mPIS.read(buf, 0, 4);
                metaData.endTime[2] = IO.bytesToInt(buf);
                mPIS.read(buf, 0, 4);
                metaData.endTime[3] = IO.bytesToInt(buf);
                mPIS.read(buf, 0, 4);
                metaData.endTime[4] = IO.bytesToInt(buf);
                mPIS.read(buf, 0, 4);
                metaData.endTime[5] = IO.bytesToInt(buf);
                mPIS.read(buf, 0, 4);
                metaData.endTime[6] = IO.bytesToInt(buf);
                mPIS.read(buf, 0, 4);
                metaData.endTime[7] = IO.bytesToInt(buf);
                mPIS.read(buf, 0, 4);
                metaData.endTime[8] = IO.bytesToInt(buf);

                //read lengths of each sensor-data set
                metaData.nSamples = new int[metaData.nSensors];
                for (int i = 0; i < metaData.nSensors; i++) {
                    buf = new byte[4];
                    mPIS.read(buf, 0, 4);
                    metaData.nSamples[i] = IO.bytesToInt(buf);
                }

                //read blocks that describe sensors
                metaData.nSensorItems = new int[metaData.nSensors];
                metaData.sensorDataTypes = new HashMap<>();
                metaData.sensorLabels = new String[metaData.nSensors];
                metaData.sensorSpecs = new String[metaData.nSensors];

                for (int i = 0; i < metaData.nSensors; i++) {
                    //read # of items / sensor
                    buf = new byte[4];
                    mPIS.read(buf, 0, 4);
                    metaData.nSensorItems[i] = IO.bytesToInt(buf);

                    //write data types of items / sensor
                    int[] sensorItems = new int[metaData.nSensorItems[i]];
                    for (int j = 0; j < metaData.nSensorItems[i]; j++) {
                        buf = new byte[4];
                        mPIS.read(buf, 0, 4);
                        sensorItems[j] = IO.bytesToInt(buf);
                    }
                    metaData.sensorDataTypes.put((i), sensorItems);

                    // read label
                    buf = new byte[2];
                    mPIS.read(buf, 0, 2);
                    int len = IO.bytesToShort(buf);
                    buf = new byte[len];
                    mPIS.read(buf, 0, len);
                    metaData.sensorLabels[i] = IO.bytesToString(buf);

                    // read specs
                    buf = new byte[2];
                    mPIS.read(buf, 0, 2);
                    len = IO.bytesToShort(buf);
                    buf = new byte[len];
                    mPIS.read(buf, 0, len);
                    metaData.sensorSpecs[i] = IO.bytesToString(buf);
                }

                //read custom text
                buf = new byte[2];
                mPIS.read(buf, 0, 2);
                int len = IO.bytesToShort(buf);
                buf = new byte[len];
                mPIS.read(buf, 0, len);
                metaData.customText = IO.bytesToString(buf);
            } catch (IOException e) {
//                log.error("Failed to read fluff data header", e);
            }
        }

        @SuppressWarnings("unchecked")
        private void ReadData() {
            try {
                //generate data object
                metaData.data = new Object[metaData.nSensors][];
                for (int i = 0; i < metaData.nSensors; i++) {
                    metaData.data[i] = new Object[metaData.nSensorItems[i]];
                    for (int j = 0; j < metaData.nSensorItems[i]; j++) {
                        switch (metaData.dataTypes[(metaData.sensorDataTypes.get(i)[j]) - 1]) {
                            case "int8":
                                metaData.data[i][j] = new ArrayList<Byte>();
                                break;
                            case "int16":
                                metaData.data[i][j] = new ArrayList<Short>();
                                break;
                            case "int32":
                                metaData.data[i][j] = new ArrayList<Integer>();
                                break;
                            case "int64":
                                metaData.data[i][j] = new ArrayList<Long>();
                                break;
                            case "float32":
                                metaData.data[i][j] = new ArrayList<Float>();
                                break;
                            case "float64":
                                metaData.data[i][j] = new ArrayList<Double>();
                                break;
                            case "bool8":
                                metaData.data[i][j] = new ArrayList<Boolean>();
                                break;
                            case "string8":
                                metaData.data[i][j] = new ArrayList<String>();
                                break;
                            case "char8":
                                metaData.data[i][j] = new ArrayList<Character>();
                                break;
                            case "uint8":
                                metaData.data[i][j] = new ArrayList<Short>();
                                break;
                            case "uint16":
                                metaData.data[i][j] = new ArrayList<Integer>();
                                break;
                            case "uint32":
                                metaData.data[i][j] = new ArrayList<Long>();
                                break;
                            case "uint64":
                                metaData.data[i][j] = new ArrayList<Long>(); //TODO: not ok!!
                                break;
                            case "dynarr":
                                metaData.data[i][j] = new ArrayList<>(); //TODO: stub!!
                                break;
                            case "statarr":
                                metaData.data[i][j] = new ArrayList<>(); //TODO: stub!!
                                break;
                            case "custom":
                                metaData.data[i][j] = new ArrayList<>(); //TODO: stub!!
                                break;
                            case "float16":
                                metaData.data[i][j] = new ArrayList<Float>();
                                break;
                        }
                    }
                }

                while (mPIS.available() > 0) {
                    byte[] buf = new byte[metaData.nBytesSID];
                    int SID;

                    //read sensor id
                    switch (metaData.nBytesSID) {
                        case 0:
                            SID = 0;
                            break;
                        default:
                            mPIS.read(buf, 0, metaData.nBytesSID);
                            SID = IO.bytesToInt(buf);
                            break;
                    }

                    //check if sensor id is valid
                    if ((SID >= 0) && (SID < metaData.nSensors)) {

                        //remember last sensor ID
                        metaData.lastSID = SID;

                        //loop through items for this sensor id
                        for (int j = 0; j < metaData.nSensorItems[SID]; j++) {
                            //read appropriate data type
                            switch (metaData.dataTypes[(metaData.sensorDataTypes.get((int) SID)[j]) - 1]) {
                                case "int8":
                                    buf = new byte[1];
                                    mPIS.read(buf, 0, 1);
                                    ((ArrayList<Byte>) metaData.data[SID][j]).add(IO.bytesToByte(buf));
                                    break;
                                case "int16":
                                    buf = new byte[2];
                                    mPIS.read(buf, 0, 2);
                                    ((ArrayList<Short>) metaData.data[SID][j]).add(IO.bytesToShort(buf));
                                    break;
                                case "int32":
                                    buf = new byte[4];
                                    mPIS.read(buf, 0, 4);
                                    ((ArrayList<Integer>) metaData.data[SID][j]).add(IO.bytesToInt(buf));
                                    break;
                                case "int64":
                                    buf = new byte[8];
                                    mPIS.read(buf, 0, 8);
                                    ((ArrayList<Long>) metaData.data[SID][j]).add(IO.bytesToLong(buf));
                                    break;
                                case "float32":
                                    buf = new byte[4];
                                    mPIS.read(buf, 0, 4);
                                    ((ArrayList<Float>) metaData.data[SID][j]).add(IO.bytesToFloat(buf));
                                    break;
                                case "float64":
                                    buf = new byte[8];
                                    mPIS.read(buf, 0, 8);
                                    ((ArrayList<Double>) metaData.data[SID][j]).add(IO.bytesToDouble(buf));
                                    break;
                                case "bool8":
                                    buf = new byte[1];
                                    mPIS.read(buf, 0, 1);
                                    ((ArrayList<Boolean>) metaData.data[SID][j]).add(IO.bytesToBool(buf));
                                    break;
                                case "string8":
                                    buf = new byte[2];
                                    mPIS.read(buf, 0, 2);
                                    int len = IO.bytesToShort(buf);
                                    buf = new byte[len];
                                    mPIS.read(buf, 0, len);
                                    ((ArrayList<String>) metaData.data[SID][j]).add(IO.bytesToString(buf));
                                    break;
                                case "char8":
                                    buf = new byte[1];
                                    mPIS.read(buf, 0, 1);
                                    ((ArrayList<Character>) metaData.data[SID][j]).add(IO.bytesToChar(buf));
                                    break;
                                case "uint8":
                                    buf = new byte[1];
                                    mPIS.read(buf, 0, 1);
                                    ((ArrayList<Short>) metaData.data[SID][j]).add(IO.bytesToShort(buf));
                                    break;
                                case "uint16":
                                    buf = new byte[2];
                                    mPIS.read(buf, 0, 2);
                                    ((ArrayList<Integer>) metaData.data[SID][j]).add(IO.bytesToInt(buf));
                                    break;
                                case "uint32":
                                    buf = new byte[4];
                                    mPIS.read(buf, 0, 4);
                                    ((ArrayList<Long>) metaData.data[SID][j]).add(IO.bytesToLong(buf));
                                    break;
                                case "uint64":
                                    buf = new byte[8];
                                    mPIS.read(buf, 0, 8);
                                    ((ArrayList<Long>) metaData.data[SID][j]).add(IO.bytesToLong(buf)); //TODO: not ok!!
                                    break;
                                case "float16":
                                    buf = new byte[2];
                                    mPIS.read(buf, 0, 2);
                                    ((ArrayList<Float>) metaData.data[SID][j]).add(IO.Float16BytesToFloat(buf)); //TODO: probably not ok!!
                                    break;

                                default:
//                                    log.error("default");
                            }
                        }
                    } else {
//                        log.error("Could not read sensor!");
                    }
                }
                findStartTime();
                findEndTime();
                displayData();
            } catch (IOException e) {
//                log.error("Failed to read data.", e);
            }
        }

        private void findStartTime() {
            //try to find sys message with unix starttime
            try {
                for (int i = 0; i < ((ArrayList<String>) metaData.data[0][1]).size(); i++) {
                    String tmpstr = ((ArrayList<String>) metaData.data[0][1]).get(i);
                    if (tmpstr.startsWith("starttime=")) {
                        metaData.uxStartTime = Long.parseLong(tmpstr.substring(tmpstr.indexOf("unix_starttime=") + 15, tmpstr.indexOf("unix_starttime=") + 25));
                        //log.debug("found starttime message: " + metaData.uxStartTime);
                        return;
                    }
                }
            } catch (Exception e) {
                try {
                    //if it didn't work, get unix start time from filename
                    metaData.uxStartTime = Long.parseLong(mFile.getName().substring(mFile.getName().indexOf(".fluff") - 13,
                            mFile.getName().indexOf(".fluff") - 3));
                    //log.debug("took uxStartTime from filename: " + metaData.uxStartTime);
                } catch (Exception ee) {
                    //todo: if you dont like taking the unix start time from the filename use the following code;
                    // however its problematic because the timezone for the conversion is not known
                    //if it didn't work convert header starttime to uxstarttime
                    Calendar c = Calendar.getInstance();
                    c.set(Calendar.YEAR, metaData.startTime[0]);
                    c.set(Calendar.MONTH, metaData.startTime[1] - 1);
                    c.set(Calendar.DAY_OF_MONTH, metaData.startTime[2]);
                    c.set(Calendar.HOUR, metaData.startTime[3]);
                    c.set(Calendar.MINUTE, metaData.startTime[4]);
                    c.set(Calendar.SECOND, metaData.startTime[5]);
                    c.set(Calendar.MILLISECOND, metaData.startTime[6]);
                    c.setTimeZone(TimeZone.getTimeZone("ECT")); // problematic because actual TZ is not known!!
                    metaData.uxStartTime = (c.getTimeInMillis() / 1000L);
                    //log.debug("took starttime from header: " + metaData.uxStartTime);
                }
            }
        }

        private void findEndTime() {
            try {
                //try to find sys-message with EndTime
                for (int i = (((ArrayList<String>) metaData.data[0][1]).size() - 1); i >= 0; i--) {
                    if (((ArrayList<String>) metaData.data[0][1]).get(i).startsWith("endtime=")) {
                        metaData.endTime[0] = Integer.parseInt((String) ((ArrayList<String>) metaData.data[0][1]).get(i).subSequence(8, 12));
                        metaData.endTime[1] = Integer.parseInt((String) ((ArrayList<String>) metaData.data[0][1]).get(i).subSequence(13, 15));
                        metaData.endTime[2] = Integer.parseInt((String) ((ArrayList<String>) metaData.data[0][1]).get(i).subSequence(16, 18));
                        metaData.endTime[3] = Integer.parseInt((String) ((ArrayList<String>) metaData.data[0][1]).get(i).subSequence(19, 21));
                        metaData.endTime[4] = Integer.parseInt((String) ((ArrayList<String>) metaData.data[0][1]).get(i).subSequence(22, 24));
                        metaData.endTime[5] = Integer.parseInt((String) ((ArrayList<String>) metaData.data[0][1]).get(i).subSequence(25, 27));
                        metaData.endTime[6] = Integer.parseInt((String) ((ArrayList<String>) metaData.data[0][1]).get(i).subSequence(28, 31));
                        metaData.endTime[7] = 0;
                        metaData.endTime[8] = 0;
                        metaData.uxEndTime = Long.parseLong((String) ((ArrayList<String>) metaData.data[0][1]).get(i).subSequence(45, 55));
						/*log.debug("EndTime: Y" + metaData.endTime[0] + " M" + metaData.endTime[1] + " D" + metaData.endTime[2]
								+ " h" + metaData.endTime[3] + " m" + metaData.endTime[4] + " s" + metaData.endTime[5]
								+ " ms" + metaData.endTime[6] + " us" + metaData.endTime[7] + " ns" + metaData.endTime[8]
								+ " ux" + metaData.uxEndTime);*/
                        return;
                    }
                }
            } catch (Exception e) {
                try {
                    //check if EndTime has not been set in header, if it has, use it to calculate unix-endtime
                    if ((metaData.endTime != null) && (metaData.startTime != null)) {
                        if ((!(Arrays.binarySearch(metaData.endTime, -1) >= 0)) && (!(Arrays.binarySearch(metaData.startTime, -1) >= 0))) {
                            Calendar c1 = Calendar.getInstance();
                            c1.set(Calendar.YEAR, metaData.startTime[0]);
                            c1.set(Calendar.MONTH, metaData.startTime[1] - 1);
                            c1.set(Calendar.DAY_OF_MONTH, metaData.startTime[2]);
                            c1.set(Calendar.HOUR, metaData.startTime[3]);
                            c1.set(Calendar.MINUTE, metaData.startTime[4]);
                            c1.set(Calendar.SECOND, metaData.startTime[5]);
                            c1.set(Calendar.MILLISECOND, metaData.startTime[6]);
                            c1.setTimeZone(TimeZone.getTimeZone("ECT"));

                            Calendar c2 = Calendar.getInstance();
                            c2.set(Calendar.YEAR, metaData.endTime[0]);
                            c2.set(Calendar.MONTH, metaData.endTime[1] - 1);
                            c2.set(Calendar.DAY_OF_MONTH, metaData.endTime[2]);
                            c2.set(Calendar.HOUR, metaData.endTime[3]);
                            c2.set(Calendar.MINUTE, metaData.endTime[4]);
                            c2.set(Calendar.SECOND, metaData.endTime[5]);
                            c2.set(Calendar.MILLISECOND, metaData.endTime[6]);
                            c2.setTimeZone(TimeZone.getTimeZone("ECT"));

                            metaData.uxEndTime = metaData.uxStartTime + (c2.getTimeInMillis() - c1.getTimeInMillis()) / 1000L;
                            return;
                        }
                    }
                } catch (Exception ee) {
                    //if that didn't work find last sensor entry and use it as endtime
                    // TODO: DIRTY Only use if you really really need that, it would be better to catch error and set endtime NaN!!
                    if (metaData.sensorSpecs[metaData.lastSID].contains("tag1=UnixTime")) {
                        //time tag is already in unixtime format
                        metaData.uxEndTime = (long) (((ArrayList) metaData.data[metaData.lastSID][0])
                                .get(((ArrayList) metaData.data[metaData.lastSID][0]).size() - 1));
                        //log.debug("used last sensor data-point as endtime: " + metaData.uxEndTime);
                        return;
                    } else {
                        //if not, try to find sensor message with conversion from sensor time to unixtime
                        for (int i = 0; i < ((ArrayList<String>) metaData.data[0][1]).size(); i++) {
                            String tmpstr = ((ArrayList<String>) metaData.data[0][1]).get(i);
                            if (tmpstr.startsWith("sensorstart=" + ((Integer) metaData.lastSID).toString())) {
                                long uxSensorStartTime = Long.parseLong(tmpstr.substring(tmpstr.indexOf("unixtime=") + 9,
                                        tmpstr.indexOf("unixtime=") + 19));
                                long sysSensorStartTime = Long.parseLong(tmpstr.substring(tmpstr.indexOf("systemclock=") + 12));
                                //make sure the unit of sysclock is microseconds
                                if (metaData.sensorSpecs[metaData.lastSID].contains("unit1=us")) {
                                    long sysEndTime = ((ArrayList<Long>) metaData.data[metaData.lastSID][0])
                                            .get(((ArrayList<Long>) metaData.data[metaData.lastSID][0]).size() - 1);
                                    metaData.uxEndTime = (sysEndTime - sysSensorStartTime) / 1000000L + uxSensorStartTime;
									/*log.debug("uxSensorStartTime=" + "  SID" + metaData.lastSID + "  " + uxSensorStartTime + "  "
											+ sysSensorStartTime + "   " + sysEndTime + "  " + metaData.uxEndTime);*/
                                    return;
                                }
                            }
                        }
                        //if it can't even find that, assume first sensor entry equals header-starttime and covert
                        long sysStartTime = ((ArrayList<Long>) metaData.data[metaData.lastSID][0]).get(0);
                        long sysEndTime = ((ArrayList<Long>) metaData.data[metaData.lastSID][0])
                                .get(((ArrayList<Long>) metaData.data[metaData.lastSID][0]).size() - 1);
                        metaData.uxEndTime = (sysEndTime - sysStartTime) / 1000000L + metaData.uxStartTime;
                    }
                }
            }
        }

        private void displayData() {
            //log.debug("Data:");
            for (int i = 0; i < metaData.nSensors; i++) {
                for (int j = 0; j < metaData.nSensorItems[i]; j++) {
					/*log.debug(i + ":" + j + ": " + metaData.sensorLabels[i] + " : "
							+ metaData.dataTypes[metaData.sensorDataTypes.get(i)[j] - 1] + " : "
							+ ((ArrayList) metaData.data[i][j]).toString());*/
                }
            }
        }
    }

    private static class IO {
        /**
         * FluffReader - a JAVA import program for .fluff files
         * Copyright (C) 2018 by Till Handel and Max Schreiber
         * Interdisciplinary Competence Center Biomedical Data Science (www. biomedical-data-science.org), Director Prof. Galina Ivanova
         * all rights reserved
         * based on pending patent no.:  DPMA 10 2017 006 361.3
         * In case of use, the following reference should be cited: DPMA 10 2017 006 361.3
         **/

        @SuppressWarnings("unused")
        public static String[] concatString(String[] a, String[] b) {
            if (a == null) return b;
            if (b == null) return a;
            String[] r = new String[a.length + b.length];
            System.arraycopy(a, 0, r, 0, a.length);
            System.arraycopy(b, 0, r, a.length, b.length);
            return r;
        }

        static ByteOrder byteOrder = ByteOrder.LITTLE_ENDIAN; // NOT TESTED!!!!

        static Byte bytesToByte(byte[] bytes) {
            ByteBuffer buf = ByteBuffer.wrap(bytes);
            buf.order(ByteOrder.LITTLE_ENDIAN);
            return buf.get();
        }

        static Boolean bytesToBool(byte[] bytes) {
            return (bytes[0] != ((byte) 0));
        }

        static Short bytesToShort(byte[] bytes) {
            ByteBuffer buf = ByteBuffer.wrap(bytes);
            buf.order(ByteOrder.LITTLE_ENDIAN);
            return buf.getShort();
        }

        static Integer bytesToInt(byte[] bytes) {
            ByteBuffer buf = ByteBuffer.allocate(4);
            buf.position(0);
            buf.put(bytes);
            buf.order(ByteOrder.LITTLE_ENDIAN);
            buf.position(0);
            return buf.getInt();
        }

        static Long bytesToLong(byte[] bytes) {
            ByteBuffer buf = ByteBuffer.wrap(bytes);
            buf.order(ByteOrder.LITTLE_ENDIAN);
            return buf.getLong();
        }

        static Float bytesToFloat(byte[] bytes) {
            ByteBuffer buf = ByteBuffer.wrap(bytes);
            buf.order(ByteOrder.LITTLE_ENDIAN);
            return buf.getFloat();
        }

        static Float Float16BytesToFloat(byte[] bytes) {
            ByteBuffer buf = ByteBuffer.allocate(4);
            buf.position(0);
            buf.put(bytes);
            buf.order(ByteOrder.LITTLE_ENDIAN);
            buf.position(0);
            int hbits = buf.getInt();


            int mant = hbits & 0x03ff;            // 10 bits mantissa
            int exp = hbits & 0x7c00;            // 5 bits exponent
            if (exp == 0x7c00)                   // NaN/Inf
                exp = 0x3fc00;                    // -> NaN/Inf
            else if (exp != 0)                   // normalized value
            {
                exp += 0x1c000;                   // exp - 15 + 127
                if (mant == 0 && exp > 0x1c400)  // smooth transition
                    return Float.intBitsToFloat((hbits & 0x8000) << 16
                            | exp << 13 | 0x3ff);
            } else if (mant != 0)                  // && exp==0 -> subnormal
            {
                exp = 0x1c400;                    // make it normal
                do {
                    mant <<= 1;                   // mantissa * 2
                    exp -= 0x400;                 // decrease exp by 1
                } while ((mant & 0x400) == 0); // while not normal
                mant &= 0x3ff;                    // discard subnormal bit
            }                                     // else +/-0 -> +/-0
            return Float.intBitsToFloat(          // combine all parts
                    (hbits & 0x8000) << 16          // sign  << ( 31 - 15 )
                            | (exp | mant) << 13);         // value << ( 23 - 10 )
        }

        static Double bytesToDouble(byte[] bytes) {
            ByteBuffer buf = ByteBuffer.wrap(bytes);
            buf.order(ByteOrder.LITTLE_ENDIAN);
            return buf.getDouble();
        }

        static Character bytesToChar(byte[] bytes) {
            ByteBuffer buf = ByteBuffer.wrap(bytes);
            buf.order(ByteOrder.LITTLE_ENDIAN);
            return buf.getChar(1);
        }

        static String bytesToString(byte[] bytes) {
            try {
                String value = new String(bytes, "ISO-8859-1");
                return value;
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                return "";
            }
        }
    }
}

