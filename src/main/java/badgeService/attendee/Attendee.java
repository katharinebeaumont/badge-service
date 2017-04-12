package badgeService.attendee;


import javax.persistence.Entity;
import javax.persistence.Id;


/*

Required structure:
{
  "firstName":"",
  "lastName":"",
  "fullName": "",
  "email": "",
  "status": "FREE" | "PENDING" | "TO_BE_PAID" | "ACQUIRED" | "CANCELLED" | "CHECKED_IN" | "EXPIRED" | "INVALIDATED" | "RELEASED" | "PRE_RESERVED",
  "uuid": "",
  "company": "",
  additionalInfo...
}

 */

/**
 * Created by katharinevoxxed on 03/02/2017.
 * Represent the attendee data that we want to print on badges
 */
@Entity
public class Attendee {

    @Id
    private String uuid;
    //Details for QR

    private String firstName;
    private String lastName;
    private String fullName;
    private String company;
    private String email;
    private String status;

    //JPA requires a default constructor.
    public Attendee() {
    }

    public Attendee(String id, String firstName, String lastName, String email) {
        this.uuid = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.fullName = firstName + " " + lastName;
        this.email = email;
        this.status = "PENDING";
    }

    @Override
    public String toString() {
        return this.fullName + ", " + this.uuid;
    }

    /**
     * Getters and setters
     */
    public String getUuid() {
        return uuid;
    }

    public void setUuid(String id) {
        this.uuid = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }


    public String getFullName() {
        if (fullName == null || fullName.isEmpty()) {
            fullName = firstName + " " + lastName;
        }
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

}
