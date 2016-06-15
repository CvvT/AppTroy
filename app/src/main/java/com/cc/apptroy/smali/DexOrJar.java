package com.cc.apptroy.smali;

/**
 * Created by CwT on 16/4/3.
 */
public class DexOrJar {

    int addr;
    String name;

    public DexOrJar(int addr, String name){
        this.addr = addr;
        this.name = name;
    }

    public DexOrJar(){}

    public void setAddr(int addr) {
        this.addr = addr;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAddr() {
        return addr;
    }

    public String getName() {
        return name;
    }
}
