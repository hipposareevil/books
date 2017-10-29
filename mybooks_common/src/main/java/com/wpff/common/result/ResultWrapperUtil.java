package com.wpff.common.result;
import java.util.List;


/**
 * Helper class to create a ResultWrapper
 *
 */
public class ResultWrapperUtil {
  
  public static int DEFAULT_OFFSET = 20;
  
  /**
   * Create a wrapper around a list of data. This will chop the incoming data
   * into the appropriate segment determined by the start and length of segment parameters.
   * 
   * @param data
   *          list of data to wrap
   * @param whereToStart
   *          Where in full list to start, zero based.
   * @param lengthOfSegment
   *          Length of data in the segment
   * @return
   */
  public static <Q> ResultWrapper<Q> createWrapper(List<Q> data,
      Integer whereToStart, 
	    Integer lengthOfSegment) {
    ResultWrapper<Q> wrapper = new ResultWrapper<>();
    
    // Check params and create defaults
    if (whereToStart == null) {
      whereToStart = 0;
    }
    if (lengthOfSegment == null) {
      // make the segment be the full data
      lengthOfSegment = DEFAULT_OFFSET;
    }

    // Verify whereToStart
    if (whereToStart >= data.size() || whereToStart < 0) {
      whereToStart = 0;
    }

    // Verify length
    if (lengthOfSegment <= 0)
      lengthOfSegment = data.size();

    int whereToEnd = whereToStart + lengthOfSegment;
    if (whereToEnd > data.size()) {
      whereToEnd = data.size();
    }
    List<Q> newData = data.subList(whereToStart, whereToEnd);

    wrapper.setData(newData);
    wrapper.setLimit(newData.size());
    wrapper.setOffset(whereToStart);
    wrapper.setTotal(data.size());

    return wrapper;
  }

  
  /**
   * Create a wrapper around the list of data. No chopping of the data is
   * performed.
   * 
   * @param data
   *          Data being returned
   * @param segment
   *          Information about the segment of data. This contains the start,
   *          length and total.
   * @return wrapper around data
   */
  public static <Q> ResultWrapper<Q> createWrapper(
        List<Q> data,
        Segment segment) {      
    ResultWrapper<Q> wrapper = new ResultWrapper<>();

    wrapper.setData(data);
    wrapper.setLimit(data.size());
    wrapper.setOffset(segment.getOffset());
    wrapper.setTotal(segment.getTotalLength());

    return wrapper;
  }
}
