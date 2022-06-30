package com.conbus.pilot.dto

import lombok.Data
import lombok.Getter

@Data
@Getter
class TestEntityDTO(
        var idx: String,
        var leftJsonStr: String,
        var rightJsonStr: String,
        var diffJsonStr: String
){
    override fun toString(): String {
        return "TestEntityDTO(idx='$idx', leftJsonStr='$leftJsonStr', rightJsonStr='$rightJsonStr', diffJsonStr='$diffJsonStr')"
    }
}