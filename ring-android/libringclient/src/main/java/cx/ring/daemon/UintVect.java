/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 3.0.8
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package cx.ring.daemon;

public class UintVect {
  private transient long swigCPtr;
  protected transient boolean swigCMemOwn;

  protected UintVect(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  protected static long getCPtr(UintVect obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        RingserviceJNI.delete_UintVect(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public UintVect() {
    this(RingserviceJNI.new_UintVect__SWIG_0(), true);
  }

  public UintVect(long n) {
    this(RingserviceJNI.new_UintVect__SWIG_1(n), true);
  }

  public long size() {
    return RingserviceJNI.UintVect_size(swigCPtr, this);
  }

  public long capacity() {
    return RingserviceJNI.UintVect_capacity(swigCPtr, this);
  }

  public void reserve(long n) {
    RingserviceJNI.UintVect_reserve(swigCPtr, this, n);
  }

  public boolean isEmpty() {
    return RingserviceJNI.UintVect_isEmpty(swigCPtr, this);
  }

  public void clear() {
    RingserviceJNI.UintVect_clear(swigCPtr, this);
  }

  public void add(long x) {
    RingserviceJNI.UintVect_add(swigCPtr, this, x);
  }

  public long get(int i) {
    return RingserviceJNI.UintVect_get(swigCPtr, this, i);
  }

  public void set(int i, long val) {
    RingserviceJNI.UintVect_set(swigCPtr, this, i, val);
  }

}
