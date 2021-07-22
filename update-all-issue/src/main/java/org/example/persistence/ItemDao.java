package org.example.persistence;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.sql.Timestamp;
import java.util.Objects;

import org.hibernate.Hibernate;

@SkipUpdateOnCondition(
        attributeName = "eventTime",
        type = "java.sql.Timestamp")
@Entity
@Table(name = "item")
public class ItemDao {
    @Id
    @Column(name = "id", nullable = false, length = -1)
    private String id;
    private String name;
    private Timestamp eventTime;
    @Transient
    private boolean modificationSkipped;


    public ItemDao(String id, String name, Timestamp eventTime) {
        this.id = id;
        this.name = name;
        this.eventTime = eventTime;
    }

    public ItemDao() {

    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean getModificationSkipped() {
        return modificationSkipped;
    }

    public void setModificationSkipped(boolean modificationSkipped) {
        this.modificationSkipped = modificationSkipped;
    }

    public Timestamp getEventTime() {
        return eventTime;
    }

    public void setEventTime(Timestamp eventTime) {
        this.eventTime = eventTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
            return false;
        }
        ItemDao itemDao = (ItemDao) o;

        return Objects.equals(id, itemDao.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
