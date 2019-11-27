package fluffUtil;

import java.time.LocalDateTime;
import java.util.Map;

public class FluffMeasurementEntriesMetaData {
	private final String fluffName;
	private final FluffSysErrMetaData sysErrData;
	private final LocalDateTime start;
	private final LocalDateTime end;
	private final Map<String, FluffMeasurementEntryMetaData> measurementEntries;

	public FluffMeasurementEntriesMetaData(String fluffName, FluffSysErrMetaData sysErrData,
                                           LocalDateTime start, LocalDateTime end,
                                           Map<String, FluffMeasurementEntryMetaData> measurementEntries) {
		this.fluffName = fluffName;
		this.sysErrData = sysErrData;
		this.start = start;
		this.end = end;
		this.measurementEntries = measurementEntries;
	}

	/**
	 * @return the fluffName
	 */
	public String getFluffName() {
		return fluffName;
	}

	/**
	 * @return the sysErrData
	 */
	public FluffSysErrMetaData getSysErrData() {
		return sysErrData;
	}

	/**
	 * @return the measurementEntries
	 */
	public Map<String, FluffMeasurementEntryMetaData> getMeasurementEntries() {
		return measurementEntries;
	}

	/**
	 * @return the start
	 */
	public LocalDateTime getStart() {
		return start;
	}

	/**
	 * @return the end
	 */
	public LocalDateTime getEnd() {
		return end;
	}

	public String getReport() {
		StringBuilder result = new StringBuilder();
		result.append("Content-report for fluff file: ").append(fluffName).append("\n");
		result.append("Covered Range: ").append(start).append(" - ").append(end).append("\n");
		result.append("Sys-Err Meta-data: \n");
		result.append("\t Previous Fluff:").append(sysErrData.getPreviousFluff()).append("\n");
		result.append("\t Next Fluff:").append(sysErrData.getNextFluff()).append("\n");
		result.append("\t Start-Time:").append(sysErrData.getStartTime()).append("\n");
		result.append("\t Time-Anchor:").append(sysErrData.getNanoAnchor()).append(" ").append(sysErrData.getTimeUnit()).append("\n");
		result.append("Contents: \n");
		for (FluffMeasurementEntryMetaData entry : measurementEntries.values()) {
			result.append("\t" + entry.getParameterID() + ": " + entry.getStartTime() + " - " + entry.getEndTime()
					+ " [Time-unit: " + entry.getTimeUnit() + "] - Entry-Count: " + entry.getEntries()
					+ " (DataTypes: " + String.join(",", entry.getParameterDataTypes()) + ") \n");
		}
		return result.toString();
	}
}