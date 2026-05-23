package org.example.attendance.model;

import jakarta.persistence.*;

@Entity
@Table(name = "student_groups", uniqueConstraints = {
        @UniqueConstraint(name = "uk_student_groups_name", columnNames = "name")
})
public class StudentGroup {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    public StudentGroup() {
    }

    public StudentGroup(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
