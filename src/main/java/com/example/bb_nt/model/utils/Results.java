package com.example.bb_nt.model.utils;

import lombok.Data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Data
public class Results {
    private List<Integer> all=Arrays.asList(0, 0);
    private List<Integer> world=Arrays.asList(0, 0);
    private List<Integer> continental=Arrays.asList(0, 0);
    private List<Integer> ct=Arrays.asList(0, 0);
    private List<Integer> scrimmage=Arrays.asList(0, 0);
    
    public void add(List<Integer> list, int pos){
        list.set(pos,list.get(pos)+1);
    }
}
