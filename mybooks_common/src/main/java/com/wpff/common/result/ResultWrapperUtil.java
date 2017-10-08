package com.wpff.common.result;
import java.util.List;


/**
 * Helper class to create a ResultWrapper
 *
 */
public class ResultWrapperUtil {
  
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
    System.out.println("version 2");
    // Check params and create defaults
    if (whereToStart == null) {
      whereToStart = 0;
    }
    if (lengthOfSegment == null) {
      // make the segment be the full data
      lengthOfSegment = -1;
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
    wrapper.setLength(newData.size());
    wrapper.setStart(whereToStart);
    wrapper.setTotalFound(data.size());

    return wrapper;
  }

}
