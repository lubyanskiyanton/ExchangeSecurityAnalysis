package ru.spb.lanton.soft.exchange.exchangesecurityanalysis.model;

public class Terror {
    
    private String userName;
    
    private String ip;
    
    private String ipCountry;
    
    private String date;
    
    private String protocol;
    
    private int countAttackIp;
    
    private int countAttackUsername;

    public Terror(String userName, String ip, String date, String protocol) {
        this.userName = userName;
        this.ip = ip;
        ipCountry = "";
        this.date = date;
        this.protocol = protocol;
        countAttackIp = 1;
        countAttackUsername = 1;
    }

    public String getUserName() {
        return userName;
    }

    public String getIp() {
        return ip;
    }

    public String getIpCountry() {
        return ipCountry;
    }

    public String getDate() {
        return date;
    }
    
    public String getProtocol() {
        return protocol;
    }

    public int getCountAttackIp() {
        return countAttackIp;
    }

    public int getCountAttackUsername() {
        return countAttackUsername;
    }
                   
    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public void setIpCountry(String ipCountry) {
        this.ipCountry = ipCountry;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void addCountAttackIp() {
        countAttackIp++;
    }
    
    public void addCountAttackUsername() {
        countAttackUsername++;
    }
                
}
