package src.server;

public class Account {
    private String name;
    private String passwd;

    public boolean isLoggedIn() {
        return loggedIn;
    }

    public void setLoggedIn(boolean loggedIn) {
        this.loggedIn = loggedIn;
    }

    private boolean loggedIn;

    public Account(String name, String passwd) {
        this.name = name;
        this.passwd = passwd;
        this.loggedIn = false;
    }

    public String getName() {
        return name;
    }

   public boolean login(String password){
        if(this.passwd.equals(password)){

            this.loggedIn = true;
            return true;
        }else{
            return false;
        }
   }
   public void logout(){
        this.loggedIn=false;
   }
}
