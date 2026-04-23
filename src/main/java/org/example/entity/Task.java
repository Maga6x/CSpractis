package org.example.entity;

import java.util.UUID;

public class Task {

    private UUID id;
    private String inputData;
    private String inputType;
    private String outputData;
    private TaskStatus status;

    public Task() {
    }

    public Task(UUID id, String inputData, String inputType, String outputData, TaskStatus status) {
        this.id = id;
        this.inputData = inputData;
        this.inputType = inputType;
        this.outputData = outputData;
        this.status = status;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getInputData() {
        return inputData;
    }

    public void setInputData(String inputData) {
        this.inputData = inputData;
    }

    public String getInputType() {
        return inputType;
    }

    public void setInputType(String inputType) {
        this.inputType = inputType;
    }

    public String getOutputData() {
        return outputData;
    }

    public void setOutputData(String outputData) {
        this.outputData = outputData;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }
}
