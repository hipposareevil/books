package com.wpff.common.result;

import java.util.List;

/**
 * Wrapper for any list of data being returned.
 * Queries for data support a 'offset' and 'limit' parameters to denote
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
  private int offset;
  
  /**
   * Number of elements in the current data list, zero based.
   */
  private int limit;
  
  /**
   * Total number of results.
   */
  private int total;

  /**
   * List of results. This may be a partial list of the full results.
   */
  private List<T> data;

 
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

  /**
   * @return the offset
   */
  public int getOffset() {
    return offset;
  }

  /**
   * @param offset the offset to set
   */
  public void setOffset(int offset) {
    this.offset = offset;
  }

  /**
   * @return the limit
   */
  public int getLimit() {
    return limit;
  }

  /**
   * @param limit the limit to set
   */
  public void setLimit(int limit) {
    this.limit = limit;
  }

  /**
   * @return the total
   */
  public int getTotal() {
    return total;
  }

  /**
   * @param total the total to set
   */
  public void setTotal(int total) {
    this.total = total;
  }
  


}
