// IRawDataManager.aidl
package com.wwc2.dvr;

// Declare any non-default types here with import statements
import com.wwc2.dvr.IRawDataCallback;

interface IRawDataManager {
    boolean register(IRawDataCallback cb,String type);
    void unregister(IRawDataCallback cb,String type);
}
