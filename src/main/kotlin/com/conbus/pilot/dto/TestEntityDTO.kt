package com.conbus.pilot.dto

import com.conbus.pilot.entity.TestEntity

class TestEntityDTO {

    constructor(testEntity: TestEntity) {
        id = testEntity.id
        result = testEntity.result
    }
    constructor()

    var id: String = ""
    var result: String = ""
    override fun toString(): String {
        return "TestEntityDTO(id='$id', result='$result')"
    }
}