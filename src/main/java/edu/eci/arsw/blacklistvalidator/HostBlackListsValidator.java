/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.eci.arsw.blacklistvalidator;

import edu.eci.arsw.spamkeywordsdatasource.HostBlacklistsDataSourceFacade;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author hcadavid
 */
public class HostBlackListsValidator {

    private static final int BLACK_LIST_ALARM_COUNT=5;
    
    /**
     * Check the given host's IP address in all the available black lists,
     * and report it as NOT Trustworthy when such IP was reported in at least
     * BLACK_LIST_ALARM_COUNT lists, or as Trustworthy in any other case.
     * The search is not exhaustive: When the number of occurrences is equal to
     * BLACK_LIST_ALARM_COUNT, the search is finished, the host reported as
     * NOT Trustworthy, and the list of the five blacklists returned.
     * @param ipaddress suspicious host's IP address.
     * @return  Blacklists numbers where the given host's IP address was found.
     */
    public List<Integer> checkHost(String ipaddress){
        
        LinkedList<Integer> blackListOcurrences=new LinkedList<>();
        
        int ocurrencesCount=0;
        
        HostBlacklistsDataSourceFacade skds=HostBlacklistsDataSourceFacade.getInstance();
        
        int checkedListsCount=0;
        
        for (int i=0;i<skds.getRegisteredServersCount() && ocurrencesCount<BLACK_LIST_ALARM_COUNT;i++){
            checkedListsCount++;
            
            if (skds.isInBlackListServer(i, ipaddress)){
                
                blackListOcurrences.add(i);
                
                ocurrencesCount++;
            }
        }
        
        if (ocurrencesCount>=BLACK_LIST_ALARM_COUNT){
            skds.reportAsNotTrustworthy(ipaddress);
        }
        else{
            skds.reportAsTrustworthy(ipaddress);
        }                
        
        LOG.log(Level.INFO, "Checked Black Lists:{0} of {1}", new Object[]{checkedListsCount, skds.getRegisteredServersCount()});
        
        return blackListOcurrences;
    }

    public List<Integer> checkHost(String ipaddress, int n){
        ArrayList<BlackListThread> threadsList = new ArrayList<>();
        LinkedList<Integer> blackListOcurrences=new LinkedList<>();
        HostBlacklistsDataSourceFacade skds=HostBlacklistsDataSourceFacade.getInstance();
        int ocurrencesCount = 0;
        int checkedListsCount = 0;

        int amountOfServers = skds.getRegisteredServersCount();
        double sizeSublists = (double) amountOfServers / n;
        int finalSize = (int) sizeSublists;
        if ((sizeSublists - amountOfServers / n) > 0.5) {
            finalSize++;
        }

        int begin = 0;
        int end = 0;
        for (int i = 0; i < n; i++) {
            begin = i * finalSize;
            end = begin + finalSize;
            if (i == n - 1) {
                end = amountOfServers;
            }
            BlackListThread blackThread = new BlackListThread(begin, end, ipaddress);
            threadsList.add(blackThread);
            blackThread.start();
        }

        for (BlackListThread blackThread : threadsList) {
            try {
                blackThread.join();
                ocurrencesCount += blackThread.getMaliciousOcurrencies();
                checkedListsCount += blackThread.getCheckedListsCount();
                blackListOcurrences.addAll(blackThread.getBlackListOcurrences());
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        if (ocurrencesCount>=BLACK_LIST_ALARM_COUNT){
            skds.reportAsNotTrustworthy(ipaddress);
        }
        else{
            skds.reportAsTrustworthy(ipaddress);
        }

        LOG.log(Level.INFO, "Checked Black Lists:{0} of {1}", new Object[]{checkedListsCount, skds.getRegisteredServersCount()});

        return blackListOcurrences;
    }

    public static int getAlarmCount () {
        return BLACK_LIST_ALARM_COUNT;
    }
    
    private static final Logger LOG = Logger.getLogger(HostBlackListsValidator.class.getName());
    
    
    
}
