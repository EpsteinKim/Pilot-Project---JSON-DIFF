package com.conbus.pilot.dto

class TestEntityDTO{

    var id: String = ""
    var leftJson: String = ""
    var rightJson: String = ""
    var diffJson: String = ""
    override fun toString(): String {
        return "TestEntityDTO(id='$id', leftJson='$leftJson', rightJson='$rightJson', diffJson='$diffJson')"
    }

}