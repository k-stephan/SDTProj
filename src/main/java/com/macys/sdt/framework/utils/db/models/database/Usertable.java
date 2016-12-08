package com.macys.sdt.framework.utils.db.models.database;


import com.macys.sdt.framework.utils.db.utils.DBConnection;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

/**
 * Created by chandra.battarai on 6/23/2016.
 */

/*
# Columns

        # User table
        # -------
        # userId
        # roleId
        # userName
        # dfltShipMethod
        # PASSWORD
        # emailAddress
        # lastVisited
        # lastViewedCart
        # lastModified
        # created
        # disabled
        # accountLocked
        # failedLoginAttemptCounter
        # FAILED_LOGIN_ATTEMPT
        # ACCOUNT_SQA_LOCKED
        # SQA_FAILED_LOGIN_ATTEMPT_COUNTER
        # sqaFailedLoginAttempt
        # orderConfSqaCounter
        # userGuid
        # siteId

*/
    /*
     # User Contact table
        # -------
        # userId
        # contactId
        # contactSeqNbr
        # contactNickname
        # dfltBillContact
        # dfltShipContact
        # lastModified
        # created
        # shipMethodCode
        # siteId
    */



public class Usertable {

    public ArrayList<User> getUserdata(String userId) throws ClassNotFoundException, SQLException {
        DBConnection dbConnection = new DBConnection();
        Connection connection = dbConnection.createConnection();
        Statement data = connection.createStatement();
        ResultSet userTableResults = data.executeQuery("Select * from user where userId = " + userId);
        ArrayList<User> userdata = new ArrayList<>();
        while (userTableResults.next()) {
            User user = new User();
            user.setUserId(userTableResults.getString("USER_ID"));
            user.setRoleId(userTableResults.getInt("ROLE_ID"));
            user.setUserName(userTableResults.getString("USER_NAME"));
            user.setDfltShipMethod(userTableResults.getString("DFLT_SHIP_METHOD"));
            user.setEmailAddress(userTableResults.getString("EMAIL_ADDRESS"));
            user.setLastVisited(userTableResults.getString("LAST_VISITED"));
            user.setLastViewedCart(userTableResults.getString("LAST_VIEWED_CART"));
            user.setLastModified(userTableResults.getString("LAST_MODIFIED"));
            user.setCreated(userTableResults.getString("CREATED"));
            user.setDisabled(userTableResults.getString("DISABLED"));
            user.setAccountLocked(userTableResults.getString("ACCOUNT_LOCKED"));
            user.setFailedLoginAttemptCounter(userTableResults.getInt("FAILED_LOGIN_ATTEMPT_COUNTER"));
            user.setSqaFailedLoginAttempt(userTableResults.getInt("SQA_FAILED_LOGIN_ATTEMPT"));
            user.setOrderConfSqaCounter(userTableResults.getInt("ORDER_CONF_SQA_COUNTER"));
            user.setUserGuid(userTableResults.getString("USER_GUID"));
            userdata.add(user);
        }
        return userdata;

    }

    public ArrayList<User_Contact> getuserContact(String userId) throws ClassNotFoundException, SQLException {
        DBConnection dbConnection = new DBConnection();
        Connection connection = dbConnection.createConnection();
        Statement data = connection.createStatement();
        ResultSet userConactResults = data.executeQuery("Select * from userContacts where userId = " + userId);
        ArrayList<User_Contact> userContacts = new ArrayList<>();
        while (userConactResults.next()) {
            User_Contact usercontact = new User_Contact();
            usercontact.setUserId(userConactResults.getString("userId"));
            usercontact.setContactId(userConactResults.getInt("CONTACT_ID"));
            usercontact.setContactSeqNbr(userConactResults.getString("CONTACT_SEQ_NBR"));
            usercontact.setContactNickname(userConactResults.getString("CONTACT_NICKNAME"));
            usercontact.setDfltBillContact(userConactResults.getString("DFLT_BILL_CONTACT"));
            usercontact.setDfltShipContact(userConactResults.getString("DFLT_SHIP_CONTACT"));
            usercontact.setLastModified(userConactResults.getString("lastModified"));
            usercontact.setCreated(userConactResults.getString("created"));
            usercontact.setShipMethodCode(userConactResults.getString("SHIP_METHOD_CODE"));
            usercontact.setSiteId(userConactResults.getString("SITE_ID"));
            userContacts.add(usercontact);
        }
        return userContacts;

    }

    public class User_Contact {
        private String userId;
        private Integer contactId;
        private String contactSeqNbr;
        private String contactNickname;
        private String dfltBillContact;
        private String dfltShipContact;
        private String lastModified;
        private String created;
        private String shipMethodCode;
        private String siteId;

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public Integer getContactId() {
            return contactId;
        }

        public void setContactId(Integer contactId) {
            this.contactId = contactId;
        }

        public String getContactSeqNbr() {
            return contactSeqNbr;
        }

        public void setContactSeqNbr(String contactSeqNbr) {
            this.contactSeqNbr = contactSeqNbr;
        }

        public String getContactNickname() {
            return contactNickname;
        }

        public void setContactNickname(String contactNickname) {
            this.contactNickname = contactNickname;
        }

        public String getDfltBillContact() {
            return dfltBillContact;
        }

        public void setDfltBillContact(String dfltBillContact) {
            this.dfltBillContact = dfltBillContact;
        }

        public String getDfltShipContact() {
            return dfltShipContact;
        }

        public void setDfltShipContact(String dfltShipContact) {
            this.dfltShipContact = dfltShipContact;
        }

        public String getLastModified() {
            return lastModified;
        }

        public void setLastModified(String lastModified) {
            this.lastModified = lastModified;
        }

        public String getCreated() {
            return created;
        }

        public void setCreated(String created) {
            this.created = created;
        }

        public String getShipMethodCode() {
            return shipMethodCode;
        }

        public void setShipMethodCode(String shipMethodCode) {
            this.shipMethodCode = shipMethodCode;
        }

        public String getSiteId() {
            return siteId;
        }

        public void setSiteId(String siteId) {
            this.siteId = siteId;
        }

        @Override
        public String toString() {
            return userId + "," + contactId + "," + contactSeqNbr + "," + contactNickname + "," + dfltBillContact + "," + dfltShipContact + "," + lastModified + "," + created + "," + shipMethodCode + "," + siteId;
        }

    }


    public class User {
        private String userId;
        private Integer roleId;
        private String userName;
        private String dfltShipMethod;
        private String emailAddress;
        private String lastVisited;
        private String lastViewedCart;
        private String lastModified;
        private String created;
        private String disabled;
        private String accountLocked;
        private Integer failedLoginAttemptCounter;
        private Integer sqaFailedLoginAttempt;
        private Integer orderConfSqaCounter;
        private String userGuid;

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public Integer getRoleId() {
            return roleId;
        }

        public void setRoleId(int roleId) {
            this.roleId = roleId;
        }

        public String getUserName() {
            return userName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }

        public String getDfltShipMethod() {
            return dfltShipMethod;
        }

        public void setDfltShipMethod(String dfltShipMethod) {
            this.dfltShipMethod = dfltShipMethod;
        }

        public String getEmailAddress() {
            return emailAddress;
        }

        public void setEmailAddress(String emailAddress) {
            this.emailAddress = emailAddress;
        }

        public String getLastVisited() {
            return lastVisited;
        }

        public void setLastVisited(String lastVisited) {
            this.lastVisited = lastVisited;
        }

        public String getLastViewedCart() {
            return lastViewedCart;
        }

        public void setLastViewedCart(String lastViewedCart) {
            this.lastViewedCart = lastViewedCart;
        }

        public String getLastModified() {
            return lastModified;
        }

        public void setLastModified(String lastModified) {
            this.lastModified = lastModified;
        }

        public String getCreated() {
            return created;
        }

        public void setCreated(String created) {
            this.created = created;
        }

        public String getDisabled() {
            return disabled;
        }

        public void setDisabled(String disabled) {
            this.disabled = disabled;
        }

        public Integer getFailedLoginAttemptCounter() {
            return failedLoginAttemptCounter;
        }

        public void setFailedLoginAttemptCounter(Integer failedLoginAttemptCounter) {
            this.failedLoginAttemptCounter = failedLoginAttemptCounter;
        }

        public Integer getSqaFailedLoginAttempt() {
            return sqaFailedLoginAttempt;
        }

        public void setSqaFailedLoginAttempt(Integer sqaFailedLoginAttempt) {
            this.sqaFailedLoginAttempt = sqaFailedLoginAttempt;
        }

        public Integer getOrderConfSqaCounter() {
            return orderConfSqaCounter;
        }

        public void setOrderConfSqaCounter(Integer orderConfSqaCounter) {
            this.orderConfSqaCounter = orderConfSqaCounter;
        }

        public String getUserGuid() {
            return userGuid;
        }

        public void setUserGuid(String userGuid) {
            this.userGuid = userGuid;
        }

        public String getAccountLocked() {
            return accountLocked;
        }

        public void setAccountLocked(String accountLocked) {
            this.accountLocked = accountLocked;
        }

        @Override
        public String toString() {
            return userId + "," + roleId.toString() + "," + userName + "," + dfltShipMethod + "," + emailAddress + "," + lastVisited + "," + lastViewedCart + "," + lastModified + "," + created + "," + disabled + "," + accountLocked + "," + failedLoginAttemptCounter.toString() + "," + sqaFailedLoginAttempt.toString() + "," + orderConfSqaCounter.toString() + "," + userGuid;
        }
    }
}


