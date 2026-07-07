package main.java.controller;

import huhu.annotation.*;

@Controller
public class A {

    @UrlMapMeth(method = "GET", value = "/home")
    public void test1() {
        System.out.println("OK");
    }

    @UrlMapMeth(method = "GET", value = "/home")
    public void test2() {
        System.out.println("POST");
    }
}






















































