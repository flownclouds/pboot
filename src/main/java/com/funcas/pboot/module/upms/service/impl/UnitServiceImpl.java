package com.funcas.pboot.module.upms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.funcas.pboot.common.enumeration.entity.DelFlag;
import com.funcas.pboot.common.util.IdWorker;
import com.funcas.pboot.module.upms.entity.Unit;
import com.funcas.pboot.module.upms.mapper.UnitMapper;
import com.funcas.pboot.module.upms.service.IUnitService;
import com.funcas.pboot.module.util.VariableUtils;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * @author funcas
 * @version 1.0
 * @date 2018年04月26日
 */
@Service("organizationService")
@Transactional(readOnly = true, rollbackFor = Exception.class)
public class UnitServiceImpl implements IUnitService {

    private final UnitMapper unitMapper;

    @Autowired
    public UnitServiceImpl(UnitMapper unitMapper) {
        this.unitMapper = unitMapper;
    }

    /**
     * 保存组织机构信息
     * @param entity
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveUnit(Unit entity) {
        if(entity.getId() != null){
            entity.setMtime(new Date());
            unitMapper.updateById(entity);
        }else{
            entity.setId(IdWorker.getId());
            entity.setCtime(new Date());
            entity.setCreatorId(VariableUtils.getPrincipal().getBaseUser().getId());
            entity.setOrgCode(this.generateOrgCode(entity.getParentId()));
            unitMapper.insert(entity);
        }

    }

    /**
     * 根据父id获取组织机构列表
     * @param pid
     * @return
     */
    @Override
    public List<Unit> getUnitByParentId(Long pid) {
        return unitMapper.selectList(new QueryWrapper<Unit>().eq("parent_id", pid));
    }

    @Override
    public List<Unit> getAllUnits() {
        return this.mergeUnit(unitMapper.selectList(new QueryWrapper<Unit>().eq("del_flag", 0).orderByAsc("sort")));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteUnit(Long id) {
        Unit unit = unitMapper.selectById(id);
        unit.setDelFlag(DelFlag.DELETED.getValue());
        unitMapper.updateById(unit);
    }

    /**
     * 根据id获取组织机构
     * @param id
     * @return
     */
    @Override
    public Unit selectOne(Long id) {
        return unitMapper.selectById(id);
    }

    /**
     * 按用户组查询关联的组织
     * @return
     */
    @Override
    public List<Unit> getGroupUnit(Long id) {
        return this.mergeUnit(unitMapper.selectGroupUnit(id));
    }

    private List<Unit> mergeUnit(List<Unit> units) {
        List<Unit> result = Lists.newArrayList();

        for (Unit entity : units) {

            Long parentId = entity.getParentId();

            if (parentId == 0) {
                recursionResource(entity, units);
                result.add(entity);
            }

        }

        return result;
    }

    private void recursionResource(Unit parent, List<Unit> units) {

        for (Unit entity : units) {
            Long parentId = entity.getParentId();

            if (parentId == 0) {
                continue;
            }

            Long id = parent.getId();

            if (parentId.equals(id)) {
                recursionResource(entity, units);
                parent.addChildren(entity);
            }
        }
    }

    /**
     * 生成orgCode
     * @param parentId 父级id
     * @return
     */
    private String generateOrgCode(Long parentId){
        String orgCode = unitMapper.genOrgCode(parentId);
        if(null == orgCode){
            Unit unit = unitMapper.selectById(parentId);
            return unit.getOrgCode() + "001";
        }
        return orgCode;
    }
}


