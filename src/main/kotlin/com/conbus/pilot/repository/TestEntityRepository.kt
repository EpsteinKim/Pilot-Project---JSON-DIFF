package com.conbus.pilot.repository

import com.conbus.pilot.entity.TestEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface TestEntityRepository: JpaRepository<TestEntity, String>  {
}