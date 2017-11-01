package com.wpff.resources;
import java.util.List;

/**
 * Container for BookBeans:
 * 
 * <code>
{
    "offset": 1,
    "limit": 1,
    "total": 2,
    "data": [
        {
            "bookId": 4,
            "rating": true,
            "data": null,
            "tags": [
                "history"
            ],
            "userBookId": 4,
            "userId": 2,
            "title": "A Swiftly Tilting Planet",
            "dateAdded": 1509573901000
        }
    ]
}
 * </code>
 *
 */
public class BookBeanList {

  private int offset;
  private int total;
  private int limit;
  
  private List<BookBean> data;

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
   * @return the data
   */
  public List<BookBean> getData() {
    return data;
  }

  /**
   * @param data the data to set
   */
  public void setData(List<BookBean> data) {
    this.data = data;
  }
}
