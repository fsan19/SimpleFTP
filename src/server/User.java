package src.server;

import java.util.ArrayList;
import java.util.Iterator;

public class User {


    private String username;
    private ArrayList<Account> accounts ;



    private boolean isAdmin;

    public User(String username) {
        this.username = username;
        this.accounts = new ArrayList<Account>();
        this.isAdmin = false;
    }
    public void addAccount(Account account){

        this.accounts.add(account);
    }

    public boolean isAdmin() {
        return isAdmin;
    }
    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }
    public String getUsername() {
        return username;
    }
    public Account findAccount(String username){
        Account acc = null;

        Iterator itr = this.accounts.iterator();

        while (itr.hasNext()){
            acc = (Account) itr.next();
            if (acc.getName().equals(username)==true){
                return acc;
            }

        }
        return null;


    }
}
