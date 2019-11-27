package fluffUtil;

import java.time.LocalDateTime;

public class FluffMeasurementEntryMetaData {
	private final String parameterID;
	private final LocalDateTime startTime;
	private final LocalDateTime endTime;
	private final int entries;
	private final String timeUnit;
	private final String[] parameterDataTypes;

	public FluffMeasurementEntryMetaData(String parameterID, LocalDateTime startTime, LocalDateTime endTime,
                                         int entries, String timeUnit, String[] parameterDataTypes) {
		this.parameterID = parameterID;
		this.startTime = startTime;
		this.endTime = endTime;
		this.entries = entries;
		this.timeUnit = timeUnit;
		this.parameterDataTypes = parameterDataTypes;
	}

	/**
	 * @return the dataTypeID
	 */
	public String getParameterID() {
		return parameterID;
	}

	/**
	 * @return the startTime
	 */
	public LocalDateTime getStartTime() {
		return startTime;
	}

	/**
	 * @return the endTime
	 */
	public LocalDateTime getEndTime() {
		return endTime;
	}

	/**
	 * @return the entries
	 */
	public int getEntries() {
		return entries;
	}

	/**
	 * @return the timeUnit
	 */
	public String getTimeUnit() {
		return timeUnit;
	}

	/**
	 * @return the parameterDataTypes
	 */
	public String[] getParameterDataTypes() {
		return parameterDataTypes;
	}


}