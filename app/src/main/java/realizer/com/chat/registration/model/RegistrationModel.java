package realizer.com.chat.registration.model;

/**
 * Created by Win on 02/09/2016.
 */
public class RegistrationModel
{
    public String UserId="";
    public String Emailid="";
    public String password="";
    public String Fname="";
    public String Lname="";
    public String ContactNo="";
    public String Dob="";
    public String MagicWord="";
    public String Gender="";
    public String ThumbnilUrl="";

    public String getThumbnilUrl() {
        return ThumbnilUrl;
    }

    public void setThumbnilUrl(String thumbnilUrl) {
        ThumbnilUrl = thumbnilUrl;
    }

    public String getMagicWord() {
        return MagicWord;
    }

    public void setMagicWord(String magicWord) {
        MagicWord = magicWord;
    }

    public String getGender() {
        return Gender;
    }

    public void setGender(String gender) {
        Gender = gender;
    }

    public String getDob() {
        return Dob;
    }

    public void setDob(String dob) {
        Dob = dob;
    }

    public String getUserId() {
        return UserId;
    }

    public void setUserId(String userId) {
        UserId = userId;
    }

    public String getEmailid() {
        return Emailid;
    }

    public void setEmailid(String emailid) {
        Emailid = emailid;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFname() {
        return Fname;
    }

    public void setFname(String fname) {
        Fname = fname;
    }

    public String getLname() {
        return Lname;
    }

    public void setLname(String lname) {
        Lname = lname;
    }

    public String getContactNo() {
        return ContactNo;
    }

    public void setContactNo(String contactNo) {
        ContactNo = contactNo;
    }


}
