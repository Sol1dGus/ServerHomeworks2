package org.example.attendance.model;

import jakarta.persistence.*;

@Entity
@Table(name = "teachers", uniqueConstraints = {
        @UniqueConstraint(name = "uk_teachers_full_name", columnNames = "full_name")
})
public class Teacher {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "full_name", nullable = false, length = 200)
    private String fullName;

    public Teacher() {
    }

    public Teacher(Long id, String fullName) {
        this.id = id;
        this.fullName = fullName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
}

