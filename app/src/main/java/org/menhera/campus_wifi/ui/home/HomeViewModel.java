package org.menhera.campus_wifi.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class HomeViewModel extends ViewModel {

    public static class ScanStatistics {
        public int numberOfAccessPoints;
        public int strongestSignal;
        public String strongestSSID;
        public String strongestBSSID;

        public ScanStatistics(int aNumber, int aSignal, String aSSID, String aBSSID) {
            numberOfAccessPoints = aNumber;
            strongestSignal = aSignal;

            String BSSID = "-";
            if (null != aBSSID && !aBSSID.equals("")) {
                BSSID = aBSSID;
            }

            strongestSSID = aSSID;
            strongestBSSID = BSSID;
        }

        public ScanStatistics() {
            this(0, 0, "<none>", "");
        }
    }

    private MutableLiveData<ScanStatistics> mStats;

    public HomeViewModel() {
        mStats = new MutableLiveData<>();
        mStats.setValue(new ScanStatistics());
    }

    public void setStatistics(ScanStatistics stats) {
        this.mStats.setValue(stats);
    }

    public LiveData<ScanStatistics> getStatistics() {
        return mStats;
    }
}