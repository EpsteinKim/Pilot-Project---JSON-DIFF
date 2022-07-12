package com.conbus.pilot.service

import com.conbus.pilot.dto.TestEntityDTO
import com.conbus.pilot.entity.TestEntity
import com.conbus.pilot.repository.TestEntityRepository
import lombok.AllArgsConstructor
import org.springframework.stereotype.Service

@Service
@AllArgsConstructor
class TestService(
        var testEntityRepository: TestEntityRepository
) {

    fun save(testEntityDTO: TestEntityDTO) {
        testEntityRepository.save(TestEntity(testEntityDTO.id, testEntityDTO.result));
    }

    fun isExist(id: String): Boolean {
        return testEntityRepository.findById(id).isPresent
    }

    fun getData(id: String): TestEntityDTO {
        val testEntity = testEntityRepository.findById(id);
        return if(testEntity.isPresent) TestEntityDTO(testEntity.get()) else TestEntityDTO()
    }
}