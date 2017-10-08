package com.wpff.common.result;

import java.util.List;

/**
 * Wrapper for any list of data being returned.
 * Queries for data support a 'start' and 'lengthOfSegment' parameters to denote
 * which slice or segment of the full data dump is requested.
 * This this is potentially a subset of the full result.
 * 
 * This will contain:
 * - List of results, this may be a partial list.
 * - Start index into the full list.
 * - Length of current list.
 */
public class ResultWrapper<T> { 
  
  /**
   * Index into the full results. To be used to determine 
   * what portion of the full results are being returned in the 
   * data list.
   */
  private int start;
  
  /**
   * Number of elements in the current data list, zero based.
   */
  private int length;
  
  /**
   * Total number of results.
   */
  private int totalFound;

  /**
   * List of results. This may be a partial list of the full results.
   */
  private List<T> data;

  /**
   * @return the start
   */
  public int getStart() {
    return start;
  }

  /**
   * @param start the start to set
   */
  public void setStart(int start) {
    this.start = start;
  }

  /**
   * @return the length
   */
  public int getLength() {
    return length;
  }

  /**
   * @param length the length to set
   */
  public void setLength(int length) {
    this.length = length;
  }

  /**
   * @return the totalFound
   */
  public int getTotalFound() {
    return totalFound;
  }

  /**
   * @param totalFound the totalFound to set
   */
  public void setTotalFound(int totalFound) {
    this.totalFound = totalFound;
  }

  /**
   * @return the data
   */
  public List<T> getData() {
    return data;
  }

  /**
   * @param data the data to set
   */
  public void setData(List<T> data) {
    this.data = data;
  }
  


}
