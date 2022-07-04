package com.conbus.pilot.dto

import lombok.Data
import lombok.Getter

@Data
@Getter
class TestEntityDTO(
        var id: String,
        var json: String,
        var result: String
){
    override fun toString(): String {
        return "TestEntityDTO(id='$id', json='$json', result='$result')"
    }
}