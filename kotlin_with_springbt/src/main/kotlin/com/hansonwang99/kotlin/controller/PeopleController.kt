package com.hansonwang99.kotlin.controller

import com.hansonwang99.kotlin.service.PeopleService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody

/**
 * Created by Administrator on 2018/1/23.
 */
@Controller
class PeopleController {
    @Autowired
    val peopleService: PeopleService? = null

    @GetMapping(value = "/hello")
    @ResponseBody
    fun hello(@RequestParam(value = "lastName") lastName: String): Any {
        val peoples = peopleService?.findByLastName(lastName)
        val map = HashMap<Any, Any>()
        map.put("hello", peoples!!)
        return map
    }

}