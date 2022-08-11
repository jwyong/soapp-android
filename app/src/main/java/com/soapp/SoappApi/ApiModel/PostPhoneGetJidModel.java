package com.soapp.SoappApi.ApiModel;

/* Created by chang on 29/08/2017. */

import java.util.ArrayList;
import java.util.List;

public class PostPhoneGetJidModel {
    private List<String> numbers;
    private String size;

    public PostPhoneGetJidModel(List<String> numbers, String size) {
        this.numbers = numbers;
        this.size = size;
    }

    public List<String> getNumbers() {
        return numbers;
    }

    public void setNumbers(ArrayList<String> numbers) {
        this.numbers = numbers;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }
}
