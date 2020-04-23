// IRawDataCallback.aidl
package com.wwc2.dvr;

// Declare any non-default types here with import statements

// Declare any non-default types here with import statements

interface IRawDataCallback {
        void onDataFrame(in ParcelFileDescriptor pfd, int size);
}
