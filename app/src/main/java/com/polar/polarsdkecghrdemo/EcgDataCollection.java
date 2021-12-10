package com.polar.polarsdkecghrdemo;

import java.time.LocalDate;
import java.util.List;

public class EcgDataCollection {

    List<Integer> ecg;
    Float hr;
    String date;
    String patientId;

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Float getHr() {
        return hr;
    }

    public void setHr(Float hr) {
        this.hr = hr;
    }

    public void setEcg(List<Integer> ecg) {
        this.ecg = ecg;
    }

    public List<Integer> getEcg() {
        return ecg;
    }
}
