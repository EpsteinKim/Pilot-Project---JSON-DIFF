package com.conbus.pilot.service

import com.conbus.pilot.dto.TestEntityDTO
import com.conbus.pilot.entity.TestEntity
import com.conbus.pilot.repository.TestEntityRepository
import lombok.AllArgsConstructor
import org.springframework.stereotype.Service

@Service
@AllArgsConstructor
class TestService (
        var testEntityRepository: TestEntityRepository
        ) {
    fun save(testEntityDTO: TestEntityDTO) {
        val testEntity = TestEntity(testEntityDTO.idx, testEntityDTO.leftJsonStr, testEntityDTO.rightJsonStr, testEntityDTO.diffJsonStr)
        testEntityRepository.save(testEntity)
    }
}