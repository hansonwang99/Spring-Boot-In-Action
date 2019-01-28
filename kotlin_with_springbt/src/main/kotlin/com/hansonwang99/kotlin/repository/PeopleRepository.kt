package com.hansonwang99.kotlin.repository

import com.hansonwang99.kotlin.entity.People
import org.springframework.data.repository.CrudRepository

/**
 * Created by Administrator on 2018/1/23.
 */

interface PeopleRepository : CrudRepository<People, Long> {
    fun findByLastName(lastName: String): List<People>?
}