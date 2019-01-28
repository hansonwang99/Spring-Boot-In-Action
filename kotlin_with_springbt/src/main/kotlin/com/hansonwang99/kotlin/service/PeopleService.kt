package com.hansonwang99.kotlin.service

import com.hansonwang99.kotlin.entity.People
import com.hansonwang99.kotlin.repository.PeopleRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

/**
 * Created by Administrator on 2018/1/23.
 */
@Service
class PeopleService : PeopleRepository {

    @Autowired
    val peopleRepository: PeopleRepository? = null


    override fun findByLastName(lastName: String): List<People>? {
        return peopleRepository?.findByLastName(lastName)
    }

    override fun <S : People?> save(entity: S): S? {
        return peopleRepository?.save(entity)
    }

    override fun <S : People?> save(entities: MutableIterable<S>?): MutableIterable<S>? {
        return peopleRepository?.save(entities)
    }

    override fun delete(entities: MutableIterable<People>?) {
    }

    override fun delete(entity: People?) {
    }

    override fun delete(id: Long?) {
    }

    override fun findAll(ids: MutableIterable<Long>?): MutableIterable<People>? {
        return peopleRepository?.findAll(ids)
    }

    override fun findAll(): MutableIterable<People>? {
        return peopleRepository?.findAll()
    }

    override fun exists(id: Long?): Boolean {
        return peopleRepository?.exists(id)!!
    }

    override fun count(): Long {
        return peopleRepository?.count()!!
    }

    override fun findOne(id: Long?): People? {
        return peopleRepository?.findOne(id)
    }

    override fun deleteAll() {
    }


}