package fluffUtil;

import java.time.LocalDateTime;

/**
 * @author Andreas KliemA
 * Created on 26.04.2018
 */
public class FluffSysErrMetaData {
	private final String previousFluff;
	private final String nextFluff;
	private final String time;
	private final long nanoAnchor;
	private final LocalDateTime startTime;
	private String timeUnit;

	public FluffSysErrMetaData(String previousFluff, String nextFluff, String time, long nanoAnchor, String timeUnit) {
		if (previousFluff != null)
			this.previousFluff = previousFluff.substring(previousFluff.indexOf("=") + 1);
		else
			this.previousFluff = null;

		if (nextFluff != null)
			this.nextFluff = nextFluff.substring(nextFluff.indexOf("=") + 1);
		else
			this.nextFluff = null;

		if (time != null) {
			this.nanoAnchor = nanoAnchor;
			this.time = time;
			this.startTime = parseStartTime();
		} else {
			this.nanoAnchor = -1;
			this.time = null;
			this.startTime = null;
		}

		this.timeUnit = timeUnit;
	}

	/**
	 * @return the previousFluff
	 */
	public String getPreviousFluff() {
		return previousFluff;
	}

	/**
	 * @return the nextFluff
	 */
	public String getNextFluff() {
		return nextFluff;
	}

	/**
	 * @return the time
	 */
	public String getTime() {
		return time;
	}

	/**
	 * @return the startTime
	 */
	public LocalDateTime getStartTime() {
		return startTime;
	}


	/**
	 * @return the nanoAnchor
	 */
	public long getNanoAnchor() {
		return nanoAnchor;
	}

	/**
	 * @return the timeUnit
	 */
	public String getTimeUnit() {
		return timeUnit;
	}

	private LocalDateTime parseStartTime() {
		int prefixCut = time.indexOf("=", 0);
		int suffixCut = time.indexOf("unix", 0);

		if (prefixCut == -1 || suffixCut == -1) {
			throw new IllegalArgumentException("Unexpected start time value found in sys_err of fluff: " + time);
		}

		String starttime = time.substring(prefixCut + 1, suffixCut - 1);
		//System.err.println("******* " + starttime);
		String[] timeFields = starttime.split("_");
		if (timeFields.length != 7) {
			throw new IllegalArgumentException("Unexpected start time value found in sys_err of fluff: " + starttime);
		}

		try {
			return LocalDateTime.of(
					Integer.parseInt(timeFields[0]),
					Integer.parseInt(timeFields[1]),
					Integer.parseInt(timeFields[2]),
					Integer.parseInt(timeFields[3]),
					Integer.parseInt(timeFields[4]),
					Integer.parseInt(timeFields[5]),
					Integer.parseInt(timeFields[6]) * 1000 * 1000);

		} catch (Exception e) {
			throw new IllegalArgumentException("Failed to parse start time value found in sys_err of fluff: " + starttime, e);
		}
	}

	public void setTimeUnit(String newUnit) {
		timeUnit = newUnit;
	}
}