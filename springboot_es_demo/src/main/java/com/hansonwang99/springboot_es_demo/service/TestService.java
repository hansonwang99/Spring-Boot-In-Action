package com.hansonwang99.springboot_es_demo.service;

import com.hansonwang99.springboot_es_demo.entity.Entity;

import java.util.List;

public interface TestService {

    void saveEntity(Entity entity);

    void saveEntity(List<Entity> entityList);

    List<Entity> searchEntity(String searchContent);
}
