package com.conbus.pilot.entity

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id

@Entity
class TestEntity(
        @Id
        var id: String,

        @Column(length=99999999)
        var result: String,
)

