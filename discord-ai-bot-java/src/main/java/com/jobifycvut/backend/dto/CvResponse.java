package com.jobifycvut.backend.dto;

public class CvResponse {
    private boolean hasCv;
    private String cvText;

    public CvResponse() {}
    public CvResponse(boolean hasCv, String cvText) {
        this.hasCv = hasCv;
        this.cvText = cvText;
    }
    public boolean isHasCv() {
        return hasCv;
    }
    public void setHasCv(boolean hasCv) {
        this.hasCv = hasCv;
    }
    public  String getCvText() {
        return cvText;
    }
    public void setCvText(String cvText) {
        this.cvText = cvText;
    }
}
