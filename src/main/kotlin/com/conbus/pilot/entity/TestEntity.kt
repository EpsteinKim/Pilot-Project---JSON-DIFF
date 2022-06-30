package com.conbus.pilot.entity

import javax.persistence.Entity
import javax.persistence.Id

@Entity
class TestEntity(
        @Id
        var idx: String,
        var leftJsonString: String,
        var rightJsonString: String,
        var diffJsonString: String
)
