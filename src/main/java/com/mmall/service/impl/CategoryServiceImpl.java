package com.mmall.service.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mmall.common.ServerResponse;
import com.mmall.dao.CategoryMapper;
import com.mmall.pojo.Category;
import com.mmall.service.ICategoryService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Set;

/**
 * Created by hasee on 2017/5/21.
 */
@Service("iCategoryService")
@Slf4j
public class CategoryServiceImpl implements ICategoryService{


    @Autowired
    private CategoryMapper categoryMapper;

    /**
     * 添加分类
     * @param categoryName
     * @param parentId
     * @return
     */
    public ServerResponse<String> addCategory(String categoryName,Integer parentId){
        if (parentId == null || StringUtils.isBlank(categoryName)){
            return ServerResponse.createByErrorMessage("添加分类参数错误！");
        }
        Category category = new Category();
        category.setName(categoryName);
        category.setParentId(parentId);
        category.setStatus(true);
        int resultCount = categoryMapper.insert(category);
        if (resultCount > 0){
            return ServerResponse.createBySuccessMessage("添加分类成功！");
        }
        return ServerResponse.createByErrorMessage("添加分类失败！");
    }

    /**
     * 更改分类名称
     * @param categoryId
     * @param categoryName
     * @return
     */
    public ServerResponse<String> setCategoryName(Integer categoryId,String categoryName){
        if (categoryId == null && StringUtils.isBlank(categoryName)){
            return ServerResponse.createByErrorMessage("更改分类名称参数错误！");
        }
        Category category = new Category();
        category.setId(categoryId);
        category.setName(categoryName);
        int resultCount = categoryMapper.updateByPrimaryKeySelective(category);
        if (resultCount > 0){
            return ServerResponse.createBySuccessMessage("分类名称更改成功！");
        }
        return ServerResponse.createByErrorMessage("分类名称更改失败！");
    }

    /**
     * 根据父节点获取下一级子节点，不递归
     * @param categoryId
     * @return
     */
    public ServerResponse<java.util.List<Category>> getChildrenParallelCategory(Integer categoryId){
        java.util.List<Category> list = categoryMapper.selectCategoryChildrenByParentId(categoryId);
        if (CollectionUtils.isEmpty(list)){
            log.info("未找到当前分类的子节点！");
        }
        return ServerResponse.createBySuccess(list);
    }

    /**
     * 递归查询本节点ID及子节点ID
     * @param categoryId
     * @return
     */
    public ServerResponse<List<Integer>> selectCategoryAndChildrenById(Integer categoryId){
        Set<Category> categorySet = Sets.newHashSet();
        findChildrenCategory(categorySet,categoryId);

        List<Integer> categoryIdList = Lists.newArrayList();
        if (categoryId != null){
            for (Category categoryItem : categorySet){
                categoryIdList.add(categoryItem.getId());
            }
        }
        return ServerResponse.createBySuccess(categoryIdList);
    }

    /**
     * 递归查询子节点
     * @param categorySet
     * @param categoryId
     * @return
     */
    public Set<Category> findChildrenCategory(Set<Category> categorySet,Integer categoryId){
        Category category = categoryMapper.selectByPrimaryKey(categoryId);
        if (category != null){
            categorySet.add(category);
        }
        //递归结束的条件是根据父节点ID查询的子节点为空
        List<Category> categoryList = categoryMapper.selectCategoryChildrenByParentId(categoryId);
        for (Category categoryItem : categoryList){
            findChildrenCategory(categorySet,categoryItem.getId());
        }
        return categorySet;
    }
}
