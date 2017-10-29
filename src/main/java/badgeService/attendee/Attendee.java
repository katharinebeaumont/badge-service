package badgeService.attendee;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import java.util.Date;


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
    @GeneratedValue
    private Integer id;

    @Column(unique = true)
    private String uuid;
    //Details for QR

    private String firstName;
    private String lastName;
    private String fullName;
    private String company;
    private String email;
    private String status = "ACQUIRED";
    private String ticketCategory = "default";

    @Column(name = "changed_since", columnDefinition="TIMESTAMP DEFAULT CURRENT_TIMESTAMP", insertable = false, updatable = false)
    private Date changedSince;

    //JPA requires a default constructor.
    public Attendee() {
    }

    public Attendee(String uuid, String firstName, String lastName, String email) {
        this.uuid = uuid;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
    }

    @Override
    public String toString() {
        return this.getFullName() + ", " + this.uuid;
    }

    /**
     * Getters and setters
     */

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
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

    public String getTicketCategory() {
        return ticketCategory;
    }

    public void setTicketCategory(String ticketCategory) {
        this.ticketCategory = ticketCategory;
    }

    public Date getChangedSince() {
        return changedSince;
    }

    public void setChangedSince(Date changedSince) {
        this.changedSince = changedSince;
    }
}
