package com.example.nahom.grocery;

/**
 * Created by Nahom on 09/28/15.
 */

public class Model{
    String name;
    int value; /* 0 -&gt; checkbox disable, 1 -&gt; checkbox enable */

    Model(String name, int value){
        this.name = name;
        this.value = value;
    }
    public String getName(){
        return this.name;
    }
    public int getValue(){
        return this.value;
    }

}