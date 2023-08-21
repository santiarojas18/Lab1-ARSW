/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.eci.arsw.blacklistvalidator;

import java.util.List;

/**
 *
 * @author hcadavid
 */
public class Main {
    
    public static void main(String a[]){
        HostBlackListsValidator hblv=new HostBlackListsValidator();
        //List<Integer> blackListOcurrences=hblv.checkHost("200.24.34.55");
        //System.out.println("The host was found in the following blacklists:"+blackListOcurrences);

        // Con un hilo
        int n = 1;

        //Con tantos hilos como procesadores
        //int n = Runtime.getRuntime().availableProcessors();
        //System.out.println("Number of processor cores: " + n);

        //Con tantos hilos como el doble de núcleos
        //int n = Runtime.getRuntime().availableProcessors() * 2;

        //Con 50 hilos
        //int n = 50;

        //Con 100 hilos
        //int n = 100;
        List<Integer> blackListOcurrences=hblv.checkHost("200.24.34.55", n);
        System.out.println("The host was found in the following blacklists:"+blackListOcurrences);
    }
    
}
