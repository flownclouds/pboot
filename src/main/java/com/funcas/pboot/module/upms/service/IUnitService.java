package com.funcas.pboot.module.upms.service;

import com.funcas.pboot.module.upms.entity.Unit;

import java.util.List;

/**
 * @author funcas
 * @version 1.0
 * @date 2018年04月26日
 */
public interface IUnitService {

    public void saveUnit(Unit entity);


    public Unit getOrganizationById(Long id);

    public List<Unit> findLeaveUnit();

    public List<Unit> getUnitByParentId(Long pid);

    public List<Unit> getAllUnits();

    public void deleteUnit(Long id);

}
