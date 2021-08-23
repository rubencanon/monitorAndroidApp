package com.polar.polarsdkecghrdemo;

import java.util.List;

public class EcgDataCollection {

    List<Integer> ecg;
    Float heatRate;



    public Float getHeatRate() {
        return heatRate;
    }

    public void setHeatRate(Float heatRate) {
        this.heatRate = heatRate;
    }

    public void setEcg(List<Integer> ecg) {
        this.ecg = ecg;
    }

    public List<Integer> getEcg() {
        return ecg;
    }
}
