package com.nowcoder.community.dao;

import org.springframework.stereotype.Repository;

//这个bean是不能被容器所管理的，容器不能扫描，需要加注解
@Repository("alphaHibernate")
public class AlphaDaoHibernateImpl implements AlphaDao{

    @Override
    public String select() {
        return "Hibernate";
    }
}
