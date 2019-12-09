package fluffUtil;


import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.util.*;

public  class FluffMetaData {
//    private static Logger log = Logger.getLogger(FluffMetaData.class);

    private int plus_fluff_endTime_minutes = 10;
    private final String fluffName;
    public int headerLen;
    public int dataIDLen;
    public int nDataTypes;
    public String[] dataTypes;
    public int nSensors;
    public int nBytesSID;
    public int[] startTime;
    // unix start value of the sensor data file
    public long uxStartTime;
    public int[] endTime;
    public long uxEndTime;
    public int[] nSamples;
    public int[] nSensorItems;
    public Map<Integer, int[]> sensorDataTypes;
    // sensor name i.e. "pho_gps" for the gps of the smart phone
    public String[] sensorLabels;
    // units of the measurement and transformation on the units like 'double(X)./4096'
    public String[] sensorSpecs;
    public String customText;
    // the real data
    // just pure numbers without information about the corresponding data-types
    public Object[][] data;
    public int lastSID = 0;

    public FluffMetaData(String fluffName) {
        this.fluffName = fluffName;
    }

    @SuppressWarnings("rawtypes")
    public String displayData() {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < nSensors; i++) {
            for (int j = 0; j < nSensorItems[i]; j++) {
                result.append(sensorLabels[i] + " : " + dataTypes[sensorDataTypes.get(i)[j] - 1]
                        + " : " + (data[i][j]).toString());
                result.append("\n");
            }
        }
        return result.toString();
    }

    @SuppressWarnings("unchecked")
    public Optional<TimeSeries> extractTimeSeriesData(String sensorName,
                                                      int typeIndex) {
        int sensorIndex = -1;
        for (int i = 0; i < nSensors; i++) {
            if (sensorLabels[i].compareTo(sensorName) == 0) {
                sensorIndex = i;
                break;
            }
        }
        if (sensorIndex == -1) {
//            if (log.isWarnEnabled()) {
//                log.warn("Failed to extract time series. unknown sensor name " + sensorName);
//            }
            return Optional.empty();
        }
        final FluffSysErrMetaData sysErrMetaData = getSysErrMetaData();
        final ArrayList<Long> timeStamps = new ArrayList<>();
        for(Double d : (ArrayList<Double>) data[sensorIndex][0]) {
            timeStamps.add(d.longValue());
        }
        if (timeStamps == null || timeStamps.isEmpty()) {
//            if (log.isWarnEnabled()) {
//                log.warn("Failed to extract time series. No timestamps found for " + sensorName);
//            }
            return Optional.empty();
        }
        Pair<LocalDateTime, LocalDateTime> datePair = getRealTime(timeStamps.get(timeStamps.size()-1));
        LocalDateTime formattedStartTime = datePair.getFirst();
        LocalDateTime formattedEndTime = datePair.getSecond();

        long anchorTime = sysErrMetaData.getNanoAnchor();

        // added
        //
        if(sysErrMetaData.getTimeUnit().equals("undefined")) {
            sysErrMetaData.setTimeUnit(sensorSpecs[0].substring(
                    sensorSpecs[0].indexOf("unit1")+6,
                    sensorSpecs[0].indexOf("unit1")+8).trim());
        }
        // added

        if (sysErrMetaData.getTimeUnit().compareTo("ns") != 0) {
            // added case "s" because getTimeUnit() returns only "s" not "ms"
            if (sysErrMetaData.getTimeUnit().compareTo("ms") == 0) {
                anchorTime = anchorTime * 1000 * 1000;
            } else if (sysErrMetaData.getTimeUnit().compareTo("s") == 0) {
                anchorTime = anchorTime * 1000 * 1000 * 1000;
            } else {
//                if (log.isWarnEnabled()) {
//                    log.warn("Failed to extract time series. Unknown time unit " + sysErrMetaData.getTimeUnit() + " found for " + sensorName);
//                }
                return Optional.empty();
            }
        }
        long multiplicator = 1;
        final String timeUnit = getTimeUnit(sensorIndex);
        if (timeUnit.compareTo("ns") != 0) {
            // added case "us" because getTimeUnit() returns only "us" not "ms"
            if (timeUnit.compareTo("ms") == 0) {
                multiplicator = 1000 * 1000;
            } else if (timeUnit.compareTo("us") == 0) {
                multiplicator = 1000;
            } else {
//                if (log.isWarnEnabled()) {
//                    log.warn("Failed to extract time series. Unknown time unit found for "
//                            + sensorName + " and index " + sensorIndex);
//                }
                return Optional.empty();
            }
        }
        final ArrayList<LocalDateTime> time = new ArrayList<>();
        final ArrayList<Double> values = new ArrayList<>();

        final ArrayList<?> dataArrayList = (ArrayList<?>) this.data[sensorIndex][typeIndex];
        for (int j = 0; j < timeStamps.size(); j++) {
            long timeStamp = timeStamps.get(j);

            final LocalDateTime formattedTimeStamp = formattedStartTime
                    .plusNanos((timeStamp * multiplicator) - sysErrMetaData.getNanoAnchor());
            //strip nanos -> frontend cannot properly handle nano resolution
            time.add(formattedTimeStamp.minusNanos(formattedTimeStamp.getNano()));
            if (!addValueToTimeSeries(dataArrayList, j, values)) {
//                if (log.isWarnEnabled()) {
//                    log.warn("Failed to extract time series. Unknown value type found for "
//                            + sensorName + " and index " + sensorIndex);
//                }
                return Optional.empty();
            }
        }
        final TimeSeries ts = new TimeSeries(sensorName + "-" + typeIndex);
        ts.setData(time, values);
        return Optional.of(ts);
    }

    private Pair<LocalDateTime, LocalDateTime> getRealTime(){
        return getRealTime(-1L);
    }

    private Pair<LocalDateTime, LocalDateTime> getRealTime(Long largestTimeStampOffset) {
        LocalDateTime start = null;
        LocalDateTime end = null;
        try {
            start = LocalDateTime.of(
                    startTime[0], startTime[1], startTime[2], startTime[3], startTime[4], startTime[5], startTime[6]*1000*1000 + startTime[7]*1000 + startTime[8]);
            end = LocalDateTime.of(
                    endTime[0], endTime[1], endTime[2], endTime[3], endTime[4], endTime[5], endTime[6]*1000*1000 + endTime[7]*1000 + endTime[8]);
        } catch (DateTimeException e) {
//            log.error("DateTime could not be parsed properly. Erratic value arrays:\n" + Arrays.toString(startTime) + "\n" + Arrays.toString(endTime));
            if(start == null) {
                start = LocalDateTime.parse("1970-01-01T00:00:00");
//                log.debug("Start is now " + start);
            }
            if(end == null) {
                if(largestTimeStampOffset > -1) {
                    System.err.println("Nanos: " + largestTimeStampOffset);
                    end = start.plusNanos(Math.abs(largestTimeStampOffset/1000));

                } else {
                    end = start.plusMinutes(10);
                }
//                log.debug("End is now " + end + "\n");
            }
        }
        return new Pair<LocalDateTime, LocalDateTime>(start, end);
    }

    private boolean addValueToTimeSeries(final ArrayList<?> data,
                                         int index,
                                         final ArrayList<Double> values) {
        Object value = data.get(index);
        if (value instanceof Double) {
            values.add((Double) value);
            return true;
        } else if (value instanceof Integer) {
            values.add(((Integer) value).doubleValue());
            return true;
        } else if (value instanceof Short) {
            values.add(((Short) value).doubleValue());
            return true;
        } else if (value instanceof Boolean) {
            values.add(1d);
            return true;
        } else if (value instanceof Byte) {
            values.add(((Byte) value).doubleValue());
            return true;
        } else if (value instanceof Float) {
            values.add(((Float) value).doubleValue());
            return true;
        } else {
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    public FluffMeasurementEntriesMetaData generateSummary(FluffMetaData metaData) {
        final Map<String, FluffMeasurementEntryMetaData> measurementEntries = new HashMap<>();
        LocalDateTime startDate = null;
        LocalDateTime endDate = null;
        final FluffSysErrMetaData sysErrMetaData = getSysErrMetaData();

        for (int i = 0; i < nSensors; i++) {
            final String parameterID = sensorLabels[i];
            final String[] stringDataTypes = new String[sensorDataTypes.get(i).length - 1];
            final ArrayList<Long> timeStamps = (ArrayList<Long>) data[i][0];
            //check if we got uint64 at first position
            if (nSensorItems[i] <= 0) {
//                if (log.isDebugEnabled()) {
//                    log.debug("No items for parameter " + parameterID + " found in fluff file: " + fluffName);
//                }
                continue;
            }

            //get all data types contained in the data of one particular sensor data
            for (int t = 1; t < sensorDataTypes.get(i).length; t++) {
                stringDataTypes[t - 1] = this.dataTypes[sensorDataTypes.get(i)[t] - 1];
            }
            if (timeStamps == null || timeStamps.isEmpty()) {
//                if (log.isDebugEnabled()) {
//						/*log.debug("Parameter " + parameterID
//								+ " does provide time series with no values. Fluff file: " + fluffName);*/
//                }
                continue;
            }
            // Set start and end time from metaData
//				Instant instantStartDate;
//				Instant instantEndDate;
//				if (metaData.uxStartTime != 0) {
//					instantStartDate = Instant.ofEpochSecond(metaData.uxStartTime);
//					startDate = instantStartDate.atZone(ZoneId.systemDefault()).toLocalDateTime();
//
//					if (metaData.uxEndTime != 0) {
//						instantEndDate = Instant.ofEpochSecond(metaData.uxEndTime);
//						endDate = instantEndDate.atZone(ZoneId.systemDefault()).toLocalDateTime();
//					} else {
//						// meta data has no EndDate
//						endDate = startDate.plus(plus_fluff_endTime_minutes, ChronoUnit.MINUTES);
//					}
//				} else {
//					log.debug("Fluff date" + metaData.fluffName + "doesn't have start time");
//				}
            Pair<LocalDateTime, LocalDateTime> datePair = getRealTime();
            startDate = datePair.getFirst();
            endDate = datePair.getSecond();

            measurementEntries.put(parameterID, new FluffMeasurementEntryMetaData(parameterID, startDate, endDate,
                    timeStamps.size(), getTimeUnit(i), stringDataTypes));
        }
        return new FluffMeasurementEntriesMetaData(fluffName, sysErrMetaData, startDate, endDate, measurementEntries);
    }

    @SuppressWarnings("unchecked")
    private FluffSysErrMetaData getSysErrMetaData() {
        String timeUnit = "undefined";

        for (int i = 0; i < nSensors; i++) {
            final String parameterID = sensorLabels[i];

            if (parameterID.compareTo("sys_err") == 0) {
                if (nSensorItems[i] > 1) {
                    timeUnit = getTimeUnit(i);
                    final ArrayList<Long> timeStamps = (ArrayList<Long>) data[i][0];
                    final ArrayList<String> sysErrData = (ArrayList<String>) data[i][1];
                    String time = sysErrData.get(0);
                    String previousFile = sysErrData.get(1);
                    String nextFile = sysErrData.get(2);
                    return new FluffSysErrMetaData(previousFile, nextFile, time, timeStamps.get(0), timeUnit);
                } else {
//                    if (log.isDebugEnabled()) {
//                        //log.debug("Bad sys_err entry found in fluff. Fluff file: " + fluffName);
//                    }
                    return new FluffSysErrMetaData(null, null, null, -1, timeUnit);
                }
            }

            if (parameterID.compareTo("sg2_err") == 0 || parameterID.compareTo("sgs_err") == 0) {
                if (nSensorItems[i] > 1) {
                    timeUnit = getTimeUnit(i);
                    final ArrayList<Long> timeStamps = (ArrayList<Long>) data[i][0];
                    final ArrayList<String> sysErrData = (ArrayList<String>) data[i][1];
                    String time = null;
                    String previousFile = null;
                    String nextFile = null;
                    time = sysErrData.get(0);

                    return new FluffSysErrMetaData(previousFile, nextFile, time, timeStamps.get(0), timeUnit);
                } else {
//                    if (log.isDebugEnabled()) {
//                        //log.debug("Bad sys_err entry found in fluff. Fluff file: " + fluffName);
//                    }
                    return new FluffSysErrMetaData(null, null, null, -1, timeUnit);
                }
            }
        }
//        if (log.isDebugEnabled()) {
//            //log.debug("No sys_err entry found in fluff. Fluff file: " + fluffName);
//        }
        return new FluffSysErrMetaData(null, null, null, -1, timeUnit);
    }

    private String getTimeUnit(int sensorIndex) {
        String timeUnit = "undefined";
        final String sensorSpec = sensorSpecs[sensorIndex];
        int unitDescIndex = sensorSpec.indexOf("unit1=");
        if (unitDescIndex != -1) {
            timeUnit = sensorSpec.substring(unitDescIndex + 6);
            timeUnit = timeUnit.substring(0, timeUnit.indexOf(" "));
        }
        return timeUnit;
    }
}

