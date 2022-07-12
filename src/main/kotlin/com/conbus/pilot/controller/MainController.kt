package com.conbus.pilot.controller

import com.conbus.pilot.dto.TestEntityDTO
import com.conbus.pilot.entity.TestEntity
import com.conbus.pilot.repository.TestEntityRepository
import com.conbus.pilot.service.TestService
import lombok.AllArgsConstructor
import lombok.RequiredArgsConstructor
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@AllArgsConstructor
class MainController(var testService: TestService){


    @GetMapping("/test")
    public fun test(@RequestParam data: String): String{
        return data;
    }

    @PostMapping("/save")
    public fun save(@RequestBody testEntityDTO: TestEntityDTO) {
        return testService.save(testEntityDTO);
    }

    @GetMapping("/isExist")
    public fun isExist(@RequestParam id: String): Boolean{
        return testService.isExist(id)
    }

    @GetMapping("/getData")
    public fun getData(@RequestParam id: String): TestEntityDTO{
        return testService.getData(id);
    }
}