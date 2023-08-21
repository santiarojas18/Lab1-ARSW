package edu.eci.arsw.blacklistvalidator;

import edu.eci.arsw.spamkeywordsdatasource.HostBlacklistsDataSourceFacade;

import java.util.LinkedList;

public class BlackListThread extends Thread {

    private int maliciousOcurrencies;
    private int begin;
    private int end;
    private String ipAddress;
    private LinkedList<Integer> blackListOcurrences;
    private int checkedListsCount;


    public BlackListThread (int begin, int end, String ipAddress) {
        maliciousOcurrencies = 0;
        this.begin = begin;
        this.end = end;
        this.ipAddress = ipAddress;
        blackListOcurrences = new LinkedList<>();
        checkedListsCount = 0;
    }
    public void run () {

        HostBlacklistsDataSourceFacade skds=HostBlacklistsDataSourceFacade.getInstance();

        for (int i=begin;i<end && maliciousOcurrencies<HostBlackListsValidator.getAlarmCount();i++){
            checkedListsCount++;

            if (skds.isInBlackListServer(i, ipAddress)){

                blackListOcurrences.add(i);

                maliciousOcurrencies++;
            }
        }
    }

    public int getMaliciousOcurrencies () {
        return maliciousOcurrencies;
    }

    public int getCheckedListsCount () {
        return checkedListsCount;
    }

    public LinkedList<Integer> getBlackListOcurrences () {
        return blackListOcurrences;
    }

}
