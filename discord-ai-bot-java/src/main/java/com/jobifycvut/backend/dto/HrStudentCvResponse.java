package com.jobifycvut.backend.dto;

public class HrStudentCvResponse {
    private Long studentId;
    private boolean hasCv;
    private String cvText;

    public HrStudentCvResponse(){}
    public HrStudentCvResponse(Long studentId, boolean hasCv, String cvText){
        this.studentId = studentId;
        this.hasCv = hasCv;
        this.cvText = cvText;
    }
    public Long getStudentId() {
        return studentId;
    }
    public boolean itHasCv() {
        return hasCv;
    }
    public String getCvText() {
        return cvText;
    }
}
