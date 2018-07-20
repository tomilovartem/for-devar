package ru.arttomilov.devartestjob;

import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserAttributes;

public class UserCredentials{
    private String login;
    private String password;
    private String phone;
    private String email;
    private String currentAccessToken;
    private String userId;

    final public static String LOGIN = "login";
    final public static String PASSWORD = "password";
    final public static String GIVEN_NAME = "given_name";
    final public static String PHONE_NUMBER = "phone_number";
    final public static String EMAIL = "email";

    public UserCredentials(String login, String password) {
        this.login = login;
        this.password = password;
        this.phone = "";
        this.email = "";
    }

    public UserCredentials(String login, String password, String phone, String email) {
        this.login = login;
        this.password = password;
        this.phone = phone;
        this.email = email;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public CognitoUserAttributes getCognitoUserAttributes(){
        CognitoUserAttributes cognitoUserAttributes = new CognitoUserAttributes();
        cognitoUserAttributes.addAttribute(GIVEN_NAME, this.getLogin());
        cognitoUserAttributes.addAttribute(PHONE_NUMBER, this.getPhone());
        cognitoUserAttributes.addAttribute(EMAIL, this.email);
        return  cognitoUserAttributes;
    }

    public String getCurrentAccessToken() {
        return currentAccessToken;
    }

    public void setCurrentAccessToken(String currentAccessToken) {
        this.currentAccessToken = currentAccessToken;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String UserIdAndAccessTokenToString() {
        return "UserId And AccessToken:\n" +
                "UserId: " + userId + '\n' +
                "AccessToken: " + currentAccessToken + '\n';
    }

    @Override
    public String toString() {
        return "User credentials:\n" +
                "login: " + login + '\n' +
                "password: " + password + '\n' +
                "phone: " + phone + '\n' +
                "email: " + email + '\n';
    }
}
