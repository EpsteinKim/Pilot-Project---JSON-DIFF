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
        val testEntity = TestEntity(
                id = testEntityDTO.id,
                leftJson = testEntityDTO.leftJson,
                rightJson = testEntityDTO.rightJson,
                diffJson = testEntityDTO.diffJson
        )
        testEntityRepository.save(testEntity);
    }

    fun isExist(id: String): Boolean {
        return testEntityRepository.findById(id).isPresent
    }

    fun getData(id: String): TestEntityDTO {
        val testEntity = testEntityRepository.findById(id);
        val testEntityDTO = TestEntityDTO()

        if(testEntity.isPresent){
            testEntityDTO.id = id
            testEntityDTO.leftJson = testEntity.get().leftJson
            testEntityDTO.rightJson = testEntity.get().rightJson
            testEntityDTO.diffJson = testEntity.get().diffJson
        }
        return testEntityDTO
    }
}