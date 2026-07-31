package com.smsc.management.app.dnd.model.entity;

import com.paicbd.smsc.utils.DndType;
import com.smsc.management.app.dnd.utils.DndStatus;
import com.smsc.management.utils.EntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "dnd_entry_list",
        uniqueConstraints = @UniqueConstraint(columnNames = "name")
)
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class DndEntryList extends EntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String name;

    @Column(name = "dnd_value", nullable = false)
    private String dndValue;

    @Enumerated(EnumType.STRING)
    @Column(name = "dnd_type", nullable = false)
    private DndType dndType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DndStatus status = DndStatus.CREATING;

    @Column(name = "comment")
    private String comment;
}
