package com.soapp.SoappApi.ApiModel;

import java.util.ArrayList;
import java.util.List;

public class ErrorModel {

    private List<ErrorModelParameter> errors;

    public ErrorModel(List<ErrorModelParameter> errors) {
        this.errors = errors;
    }

    public ErrorModel() {
        this.errors = new ArrayList<>();
    }

    public List<ErrorModelParameter> getErrors() {
        return errors;
    }

    public void setErrors(List<ErrorModelParameter> errors) {
        this.errors = errors;
    }

    public void addList(ErrorModelParameter parameter){
        this.errors.add(parameter);
    }
}

