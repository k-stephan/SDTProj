package com.macys.sdt.framework.utils.db.models;

import com.macys.sdt.framework.utils.TestUsers;
import com.macys.sdt.framework.utils.db.utils.EnvironmentDetails;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.StringWriter;

public class UserService {

    private static final String SERVICE_ENDPOINT = "customer/v1/users?_body=true";

    public static String createUser() {
        String userId = null;
        try {
            String serviceUrl = getServiceURL();
            System.out.println(getServiceURL());
            PostMethod post = new PostMethod(serviceUrl);
            String postdata = userServiceXML();
            post.setRequestEntity(new StringRequestEntity(postdata, "application/xml", "UTF-8"));
            HttpClient httpclient = new HttpClient();
            int result = httpclient.executeMethod(post);
            System.out.println("Response status code: " + result);
            //System.out.println("Response body: ");
            String response = post.getResponseBodyAsString();
            post.releaseConnection();
            JSONObject obj = new JSONObject(response);
            userId = obj.getJSONObject("user").getJSONObject("userPasswordHint").get("userID").toString();
        } catch (IOException | JSONException e) {
            System.err.println("Unable to create a user: " + e);
        }
        return userId;
    }

    public static String userServiceXML() {
        try {
            DocumentBuilderFactory dFact = DocumentBuilderFactory.newInstance();
            DocumentBuilder build = dFact.newDocumentBuilder();
            Document doc = build.newDocument();
            Element root = doc.createElement("user");
            doc.appendChild(root);
            Element dob = doc.createElement("dateOfBirth");
            dob.appendChild(doc.createTextNode("1991-07-22"));
            root.appendChild(dob);
            Element gender = doc.createElement("gender");
            gender.appendChild(doc.createTextNode(TestUsers.generateRandomGender()));
            root.appendChild(gender);
            Element subscription = doc.createElement("subscribedToNewsLetter");
            subscription.appendChild(doc.createTextNode("true"));
            root.appendChild(subscription);
            Element userpasswordhint = doc.createElement("userPasswordHint");
            root.appendChild(userpasswordhint);
            Element id = doc.createElement("id");
            id.appendChild(doc.createTextNode("9"));
            userpasswordhint.appendChild(id);
            Element answer = doc.createElement("answer");
            answer.appendChild(doc.createTextNode("white"));
            userpasswordhint.appendChild(answer);
            Element profileaddress = doc.createElement("profileAddress");
            root.appendChild(profileaddress);
            Element addressid = doc.createElement("id");
            addressid.appendChild(doc.createTextNode("1234"));
            profileaddress.appendChild(addressid);
            Element profileattention = doc.createElement("attention");
            profileattention.appendChild(doc.createTextNode("1234"));
            profileaddress.appendChild(profileattention);
            Element seqnumber = doc.createElement("sequenceNumber");
            seqnumber.appendChild(doc.createTextNode("11"));
            profileaddress.appendChild(profileattention);
            Element firstname = doc.createElement("firstName");
            firstname.appendChild(doc.createTextNode(TestUsers.generateRandomFirstName()));
            profileaddress.appendChild(firstname);
            Element lastname = doc.createElement("lastName");
            lastname.appendChild(doc.createTextNode(TestUsers.generateRandomLastName()));
            profileaddress.appendChild(lastname);
            Element middlename = doc.createElement("middleName");
            middlename.appendChild(doc.createTextNode("Shekhar"));
            profileaddress.appendChild(middlename);
            Element addressline1 = doc.createElement("addressLine1");
            addressline1.appendChild(doc.createTextNode("address_line_1"));
            profileaddress.appendChild(addressline1);
            Element city = doc.createElement("city");
            city.appendChild(doc.createTextNode("Hyderabad"));
            profileaddress.appendChild(city);
            Element state = doc.createElement("state");
            state.appendChild(doc.createTextNode("AP"));
            profileaddress.appendChild(state);
            Element zipCode = doc.createElement("zipCode");
            zipCode.appendChild(doc.createTextNode("32701"));
            profileaddress.appendChild(zipCode);
            Element countryCode = doc.createElement("countryCode");
            countryCode.appendChild(doc.createTextNode("US"));
            profileaddress.appendChild(countryCode);
            Element email = doc.createElement("email");
            email.appendChild(doc.createTextNode((TestUsers.generateRandomEmail(16))));
            profileaddress.appendChild(email);
            Element bestPhone = doc.createElement("bestPhone");
            bestPhone.appendChild(doc.createTextNode(TestUsers.generateRandomPhoneNumber()));
            profileaddress.appendChild(bestPhone);
            Element primaryFlag = doc.createElement("primaryFlag");
            primaryFlag.appendChild(doc.createTextNode("true"));
            profileaddress.appendChild(primaryFlag);
            Element loginCredentials = doc.createElement("loginCredentials");
            root.appendChild(loginCredentials);
            Element password = doc.createElement("password");
            password.appendChild(doc.createTextNode("macys123"));
            loginCredentials.appendChild(password);
            Element userRequestAction = doc.createElement("userRequestAction");
            userRequestAction.appendChild(doc.createTextNode("ADD"));
            root.appendChild(userRequestAction);

            TransformerFactory tFact = TransformerFactory.newInstance();
            Transformer trans = tFact.newTransformer();

            StringWriter writer = new StringWriter();
            StreamResult result = new StreamResult(writer);
            DOMSource source = new DOMSource(doc);
            trans.transform(source, result);
            //System.out.println(writer.toString());
            return writer.toString();
        } catch (TransformerException | ParserConfigurationException e) {
            System.err.println("Unable to get user XML: " + e);
        }
        return null;
    }

    private static String getServiceURL() {
        return "http://" + EnvironmentDetails.otherApp("mspcustomer").ipAddress + ":8080/api/" + SERVICE_ENDPOINT;
    }

}





