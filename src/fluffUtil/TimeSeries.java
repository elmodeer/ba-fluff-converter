package fluffUtil;

//import org.apache.log4j.Logger;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 
 * @author Andreas Kliem, Nikolas Schinkels
 * Created on 01.02.2018
 *
 */
public class TimeSeries {
	
//	private static final Logger log = Logger.getLogger(TimeSeries.class);
	
	public static final TimeSeries EMPTY = new TimeSeries("EMPTY");
	static {
		EMPTY.xAxis = new LocalDateTime[] {};
		EMPTY.yAxis = new Double[] {};
	}

    private final String name; //referenziert den Parameter-namen
    private LocalDateTime[] xAxis; //referenziert den Parameter-namen
    private Double[] yAxis; //referenziert den Parameter-namen
    private boolean finalArea; //Markierung ob finale oder temporäre Daten
	
    //================================================================================
    // Constructor
    //================================================================================
    
    public TimeSeries(String name) {
    	this.name = name;
    }

    public TimeSeries(String name, double value) {
        this.name = name;
    }

    //================================================================================
    // Utilities
    //================================================================================
    
//    public void concat(TimeSeries ts) {
//    	this.xAxis = ArrayUtils.addAll(xAxis, ts.getxAxis());
//    	this.yAxis = (Double[]) ArrayUtils.addAll(yAxis, ts.getyAxis());
//    }

    //================================================================================
    // Getters and Setters
    //================================================================================

    public void setData(LocalDateTime[] time, Double[] values) {
    	if (time.length != values.length) {
//    		log.error("time: " + time.length + " entries -- values: " + values.length + " entries");
    		throw new IllegalArgumentException("Cannot set data for time series with different length of x and y axis.");
    	}
    	
    	this.yAxis = values;
    	this.xAxis = time;
    }
    
    public void setData(List<LocalDateTime> time, List<Double> values) {
		Double[] unboxedY = new Double[values.size()];
		int count = 0;
		for (Double val : values)
			unboxedY[count++]=val;

		setData(time.toArray(new LocalDateTime[time.size()]), unboxedY);
    }
    
    public String getName() {
        return name;
    }


    public LocalDateTime[] getxAxis() {
        return xAxis;
    }

    public void setxAxis(LocalDateTime[] xAxis) {
        this.xAxis = xAxis;
    }

    public Double[] getyAxis() {
        return yAxis;
    }

    public void setyAxis(Double[] yAxis) {
        this.yAxis = yAxis;
    }

    public boolean isFinalArea() {
        return finalArea;
    }

    public void setFinalArea(boolean finalArea) {
        this.finalArea = finalArea;
    }

    // do we need a TimeRange hier ?
//    public Optional<TimeRange> getCoveredRange() {
//    	if (xAxis == null || xAxis.length == 0)
//    		return Optional.empty();
//
//    	return Optional.of(new TimeRange(xAxis[0], xAxis[xAxis.length-1]));
//    }
    
    public int size() {
    	if (xAxis == null)
    		return 0;
    	
    	return xAxis.length;
    }
}