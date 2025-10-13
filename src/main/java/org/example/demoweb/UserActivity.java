package org.example.demoweb;
import java.time.LocalDate;
public class UserActivity {
    public LocalDate date;
    public int imActiveFlag;

    // Constructor
    public UserActivity(LocalDate date, int imActiveFlag) {
        this.date = date;
        this.imActiveFlag = imActiveFlag;
    }

    // Getter và Setter cho `date`
    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    // Getter và Setter cho `imActiveFlag`
    public int getImActiveFlag() {
        return imActiveFlag;
    }

    public void setImActiveFlag(int imActiveFlag) {
        this.imActiveFlag = imActiveFlag;
    }

    @Override
    public String toString() {
        return "UserActivity{" +
                "date=" + date +
                ", imActiveFlag=" + imActiveFlag +
                '}';
    }
}
