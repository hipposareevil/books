package com.wpff.common.util;

import java.util.Date;

/**
 * Util to do timing. 
 * 
 * Example:
 * <code>
 * TimeIt timer = TimeIt.mark();
 *  // do some work
 * System.out.println(timer.done("done with work");
 *  // results in "done with work 135ms."
 * </code>
 */
public class TimeIt {
  
  private long start = 0L;
  private long end = 0l;
  
  private TimeIt() {
    start = new Date().getTime();
  }
  
  public static TimeIt mark() {
    return new TimeIt();
  }
  
  public String done(String msg) {
    end = new Date().getTime();
    
    return msg+ " " + (end - start) + "ms.";
  }

}
