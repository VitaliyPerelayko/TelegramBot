package com.expense;

public interface BuildVars {
    Boolean debug = true;
    Boolean useWebHook = false;
    int PORT = 8443;
    String EXTERNALWEBHOOKURL = "https://expense-app-tlegram-bot.herokuapp.com:" + PORT;
    String INTERNALWEBHOOKURL = "https://localhost:" + PORT; // https://(xyz.)localip/domain(.tld)
    String pathToCertificatePublicKey = "./YOURPEM.pem"; //only for self-signed webhooks
    String pathToCertificateStore = "./YOURSTORE.jks"; //self-signed and non-self-signed.
    String certificateStorePassword = "yourpass"; //password for your certificate-store

    String CONSUMER_KEY = "3MVG9n_HvETGhr3CpZq72tygPDklqrcrr_9jWyUTNPnkdsOjR7UWiIVffZRuD8NfGob7qezMVNMhDJZ8_jAl_";
    String CONSUMER_SECRET = "0B45CA3B8DC15EA571F76A97E3F25CC5E7408654968DEE85221C1EC430D0B9B7";
    String LOGIN_URL = "https://login.salesforce.com";
    String GRANTSERVICE = "/services/oauth2/token?grant_type=password";
    String USERNAME = "ant1989alo@expense.com";
    String PASSWORD = "123qwa12ohDedHuhPt7oFezvFA8benCSL";

    String REST_URL = "/services/apexrest/telegram/";


//    public static final String DirectionsApiKey = "<your-api-key>";
//
//    public static final String TRANSIFEXUSER = "<transifex-user>";
//    public static final String TRANSIFEXPASSWORD = "<transifex-password>";
//    public static final List<Integer> ADMINS = new ArrayList<>();

    String pathToLogs = "./";
}
