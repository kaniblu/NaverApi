package com.kaniblu.naver.api;

import java.util.logging.Level;

import com.kaniblu.naver.http.HttpForm;
import com.kaniblu.naver.http.HttpHeaders;
import com.kaniblu.naver.http.HttpResult;

public class AuthorizedConnection extends Connection {	
    private String mUsername;
    private String mPassword;
    
    public AuthorizedConnection() {
    	super();
    }
    
    public AuthorizedConnection(String name, String password) {
    	super();
    	
    	this.mUsername = name;
    	this.mPassword = password;
    }
    
    public void login() throws InternalException, LoginException, ServerException
    {
        if (mUsername == null || mPassword == null)
            throw new LoginException("Username and password are required.");

        requestKeys();
        requestLogin();
    }

	public void setPassword(String password)
	{
	    this.mPassword = password;
	}

	public void setUsername(String username)
	{
	    this.mUsername = username;
	}

	protected HttpForm generateLoginRequestForm()
	{
		HttpForm formContent = new HttpForm();
	    String encrypted = getEncryptedCredentials();
	
	    if (encrypted == null) {
	        logger.log(Level.SEVERE, "Failed to encrypt credentials.");
	        return null;
	    }
	
	    formContent.put("enctp", "1");
	    formContent.put("encpw", encrypted);
	    formContent.put("encnm", mKeySet.keyName);
	    formContent.put("locale", "en_US");
	    formContent.put("smart_LEVEL", "1");
	    formContent.put("url", "http://www.naver.com");
	
	    return formContent;
	}

	protected HttpHeaders generateLoginRequestHeader()
	{
	    HttpHeaders header = new HttpHeaders();
	    header.put("Content-Type", "application/x-www-form-urlencoded");
	    header.put("Host", "nid.naver.com");
	
	    return header;
	}

	protected String getEncryptedCredentials()
	{
	    RSA rsa = new RSA();
	    String encrypt;
	    rsa.setPublic(mKeySet.eValue, mKeySet.nValue);
	
	    encrypt = rsa.encrypt(getCharCode(mKeySet.sessionKey) + mKeySet.sessionKey + getCharCode(mUsername) + mUsername + getCharCode(mPassword) + mPassword);
	    return encrypt;
	}

	protected void requestLogin() throws InternalException, LoginException, ServerException
	{
	    HttpHeaders header = generateLoginRequestHeader();
	    HttpForm formContent = generateLoginRequestForm();
	
	    if (header == null || formContent == null) {
	        logger.log(Level.SEVERE, "Failed to generate header or form content.");
	        throw new InternalException();
	    }
	
	    HttpResult loginResult = requestPost("https://nid.naver.com/nidlogin.login", header, formContent);
	    if (!loginResult.isStatusOk()) {
	        logger.log(Level.FINE, "Login failed.");
	        throw new InternalException();
	    }
	
	    if (mCookies.contains("NID_SES") && mCookies.contains("NID_AUT")) {
	        logger.log(Level.INFO, "Login was successful.");
	        return;
	    }
	
	    if (loginResult.hasContent()) {
	        String content = loginResult.getContentAsString();
	        String msg = extractErrorMsg(content);
	
	        if (msg == null) {
	            logger.log(Level.SEVERE, "Couldn't find error message in the naver response.");
	            throw new InternalException();
	        } else {
	            logger.log(Level.FINE, "Server returned the following msg: " + msg);
	            throw new LoginException(msg);
	        }
	    } else {
	        logger.log(Level.WARNING, "No content was returned by the server.");
	        throw new InternalException();
	    }
	}

	public void requestCookies() throws InternalException, ServerException
	{
	    HttpResult result = requestGet("https://nid.naver.com/nidlogin.login", null, null);
	
	    if (result.statusCode / 100 != 2) {
	        logger.log(Level.SEVERE, "Naver is not available.");
	        throw new InternalException("Naver is not available.");
	    }
	}

	protected static String getCharCode(String s)
	{
	    return String.valueOf((char)s.length());
	}
}
