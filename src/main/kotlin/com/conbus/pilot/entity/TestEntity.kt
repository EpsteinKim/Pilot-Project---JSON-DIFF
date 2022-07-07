package com.conbus.pilot.entity

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id

@Entity
class TestEntity(
        @Id
        var id: String,

        @Column(length=99999)
        var leftJson: String,

        @Column(length=99999)
        var rightJson: String,

        @Column(length=99999)
        var diffJson: String
)
