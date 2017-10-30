package com.wpff.common.result;

public class Segment {
  
  int DEFAULT_OFFSET = 0;
  int DEFAULT_LIMIT = 20;
  
  /**
   * Offset (start) of data segment
   */
  Integer offset;
  
  /**
   * Limit (length) of data segment
   */
  Integer limit;
  
  /**
   * Number of total datum
   */
  Long totalLength = 0L;

  public Segment(Integer offset, Integer limit) {
    super();
    if (offset == null || offset < 0) {
      offset = DEFAULT_OFFSET;
    }
    if (limit == null || limit < 0) {
      limit = DEFAULT_LIMIT;
    }

    this.offset = offset;
    this.limit = limit;
  }

  /**
   * @return the offset
   */
  public Integer getOffset() {
    return offset;
  }

  /**
   * @param offset the offset to set
   */
  public void setOffset(Integer offset) {
    this.offset = offset;
  }


  /**
   * @return the limit
   */
  public Integer getLimit() {
    return limit;
  }

  /**
   * @param limit the limit to set
   */
  public void setLimit(Integer limit) {
    this.limit = limit;
  }

  /**
   * @return the totalLength
   */
  public Long getTotalLength() {
    return totalLength;
  }

  /**
   * @param totalLength the totalLength to set
   */
  public void setTotalLength(Long totalLength) {
    this.totalLength = totalLength;
  }

}
